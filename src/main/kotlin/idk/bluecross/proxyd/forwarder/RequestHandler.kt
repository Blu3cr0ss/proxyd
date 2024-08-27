package idk.bluecross.proxyd.forwarder

import idk.bluecross.proxyd.forwarder.http.HttpResponseFrame
import idk.bluecross.proxyd.service.IProxyProviderService
import idk.bluecross.proxyd.service.ProxyProviderService
import idk.bluecross.proxyd.util.getLogger
import java.io.*
import java.net.*
import java.util.concurrent.CompletableFuture
import kotlin.concurrent.thread
import kotlin.jvm.Throws

class RequestHandler(
    private var clientSocket: Socket,
    clientSocketTimeout: Int?,
    private val connectTimeout: Int,
    private val readTimeout: Int?,
    private val proxyProvider: IProxyProviderService,
) : Runnable {
    private var bufferedText = StringBuilder()
    private var allowHttpProxy = true

    init {
        if (clientSocketTimeout != null) clientSocket.soTimeout = clientSocketTimeout
    }

    @Throws(IOException::class)
    override fun run() {
        do {
            val byte = clientSocket.getInputStream().read()
            bufferedText.append(Char(byte))
        } while (Char(byte).let { it != '\n' && it != '\r' })

        if (!Regex("HTTP/\\d\\.\\d\$").containsMatchIn(bufferedText.toString())) allowHttpProxy = false

        handleRequest()
    }

    private fun forwardStream(inputStream: InputStream, outputStream: OutputStream, bufferSize: Int = 1024) {
        val buffer = ByteArray(bufferSize)
        var read: Int
        do {
            read = inputStream.read(buffer)
            if (read > 0) {
                outputStream.write(buffer, 0, read)
                if (inputStream.available() < 1) {
                    outputStream.flush()
                }
            }
        } while (read >= 0)
    }

    private fun handleRequest() {
        val socket = Socket()
        try {
            val chosenProxy = if (!allowHttpProxy) proxyProvider.getSocksProxy().get()
            else proxyProvider.getProxy().get()
            getLogger().debug("Serving ${bufferedText.toString()} with $chosenProxy")
            if (readTimeout != null) socket.soTimeout = readTimeout
            socket.connect(
                InetSocketAddress(chosenProxy.ip, chosenProxy.port),
                connectTimeout
            )
            socket.getOutputStream().write(bufferedText.toString().encodeToByteArray())
            val exInThreads = arrayOfNulls<Throwable>(2)
            val cs = thread(name = "Client->Server") {
                runCatching {
                    forwardStream(clientSocket.getInputStream(), socket.getOutputStream())
                }.onFailure {
                    exInThreads[0] = it
                }
            }
            val sc = thread(name = "Server->Client") {
                runCatching {
                    forwardStream(socket.getInputStream(), clientSocket.getOutputStream())
                }.onFailure {
                    exInThreads[1] = it
                }
            }
            sc.join()
            cs.join()
            exInThreads.firstOrNull { it != null }?.let { throw it }
        } catch (e: Exception) {
            returnException(e)
        }
        socket.close()
        clientSocket.close()
    }


    private fun returnException(e: Throwable) {
        clientSocket.getOutputStream().write(HttpResponseFrame().apply {
            statusCode = 500
            statusText = e.message
            body = e.stackTraceToString()
        }.toString().encodeToByteArray())
        clientSocket.getOutputStream().flush()
    }
}