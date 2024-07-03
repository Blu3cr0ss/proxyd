package idk.bluecross.proxyd.forwarder

import idk.bluecross.proxyd.util.getLogger
import java.io.*
import java.net.*
import java.net.Proxy
import kotlin.concurrent.thread
import kotlin.jvm.Throws

class HttpHttpsRequestHandler(
    private var clientSocket: Socket,
    clientSocketTimeout: Int?,
    private val connectTimeout: Int,
    private val readTimeout: Int?,
    private val proxy: Proxy?
) : Runnable {

    val logger = getLogger()

    init {
        if (clientSocketTimeout != null) clientSocket.soTimeout = clientSocketTimeout
    }

    private val usingProxy = proxy != null

    @Throws(IOException::class)
    override fun run() {
        val clientRequest = HttpRequest.parseRequest(clientSocket.getInputStream())
        clientRequest.headers.remove("Accept-Encoding") // TODO handle encoded requests

        if (clientRequest.method == "CONNECT") {
            logger.debug("HTTPS CONNECT for ${clientRequest.url()} with ${proxy?.type()}${proxy?.address()}")
            handleRequest(clientRequest)
        } else {
            logger.debug("HTTP ${clientRequest.method} for ${clientRequest.url()} with ${proxy?.type()}${proxy?.address()}")
            handleRequest(clientRequest)
        }
    }

    private fun forwardStream(inputStream: InputStream, outputStream: OutputStream, bufferSize: Int = 1024) {
        runCatching {
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
            } while (read >= 0 && !clientSocket.isClosed)
        }.onFailure { e ->
            when (e) {
                is SocketTimeoutException -> {
                    logger.debug(
                        "SocketTimeoutException ${
                            if (inputStream == clientSocket.getInputStream())
                                "client -> server" else "client <- server"
                        }"
                    )
                }

                else -> e.printStackTrace()
            }
        }

    }

    private fun handleRequest(clientRequest: HttpRequest) {
        try {
            val https = clientRequest.method == "CONNECT"
            val proxyToServerSocket = Socket()
            proxyToServerSocket.connect(
                if (!usingProxy) {
                    val split =
                        clientRequest.headers["Host"]?.split(":") ?: clientRequest.headers["host"]?.split(":") ?: run {
                            clientSocket.getOutputStream().write(HttpResponse().apply {
                                statusCode = 400
                                statusText = "Bad Request"
                                body = "No Host or host header"
                            }.toString().encodeToByteArray())
                            clientSocket.getOutputStream().close()
                            Thread.currentThread().stop()
                            return
                        }
                    InetSocketAddress(
                        InetAddress.getByName(split[0]),
                        split.getOrNull(1)?.toInt() ?: if (https) 443 else 80
                    )
                } else InetSocketAddress(
                    (proxy!!.address() as InetSocketAddress).address,
                    (proxy.address() as InetSocketAddress).port
                ), connectTimeout
            )
            if (readTimeout != null) proxyToServerSocket.soTimeout = readTimeout
            if (https) {
                if (!usingProxy) {
                    clientSocket.getOutputStream().write(HttpResponse().apply {
                        statusCode = 200
                        statusText = "Connection established"
                        headers["Proxy-Agent"] = "ProxyD-Forwarder/1.0"
                    }.toString().encodeToByteArray())
                    clientSocket.getOutputStream().flush()
                } else proxyToServerSocket.getOutputStream()
                    .write(clientRequest.toString().encodeToByteArray())

                thread {
                    forwardStream(clientSocket.getInputStream(), proxyToServerSocket.getOutputStream())
                }
                forwardStream(proxyToServerSocket.getInputStream(), clientSocket.getOutputStream())
            } else {
                proxyToServerSocket.getOutputStream()
                    .write(clientRequest.toString().encodeToByteArray())

                forwardStream(proxyToServerSocket.getInputStream(), clientSocket.getOutputStream())
            }

            clientSocket.getOutputStream().close()
            proxyToServerSocket.getOutputStream().close()
        } catch (e: Exception) {
            e.printStackTrace()
            clientSocket.getOutputStream().write(HttpResponse().apply {
                body = e.message
                statusCode = 502
                statusText = "Bad Gateway"
            }.toString().encodeToByteArray())
            clientSocket.getOutputStream().close()
        }
    }

    private abstract class HttpBase {
        var version = "HTTP/1.1"
        var headers = hashMapOf<String, String>()
        var body: String? = null
    }

    private class HttpRequest : HttpBase() {
        var method: String? = null
        var target: String? = null
        fun url(): String? {
            return if (
                target?.startsWith("http://") == true ||
                target?.startsWith("https://") == true ||
                target?.startsWith("www.") == true
            ) {
                target
            } else "http://$target"
        }

        override fun toString(): String = "$method $target $version\n" +
                headers.map { "${it.key}: ${it.value}" }.joinToString("\n") +
                "\n\n" +
                (body ?: "")

        companion object {
            fun parseRequest(inputStream: InputStream): HttpRequest {
                val br = inputStream.bufferedReader()
                val httpRequest = HttpRequest()

                br.readLine()?.split(" ")?.apply {
                    httpRequest.method = this[0]
                    httpRequest.target = this[1]
                    httpRequest.version = this[2]
                } ?: Thread.currentThread().stop()

                var bodyStarted = false
                var line: String? = null
                while (br.readLine()?.also { line = it } != null) {
                    if (line!!.isBlank()) {
                        if (!httpRequest.headers.containsKey("Content-Length")) break
                        bodyStarted = true
                        continue
                    }
                    if (!bodyStarted) with(line!!.split(": ")) {
                        httpRequest.headers[this[0]] = this[1]
                    }
                    else httpRequest.body += line
                }
                return httpRequest
            }

            fun parseRequest(str: String) {
                val httpRequest = HttpRequest()
                with(str.lines()) {
                    val iterator = iterator()
                    with(iterator.next().split(" ")) {
                        httpRequest.method = this[0]
                        httpRequest.target = this[1]
                        httpRequest.version = this[2]
                    }
                    var bodyStarted = false
                    while (iterator.hasNext()) {
                        val line = iterator.next()
                        if (line.isBlank()) {
                            if (!httpRequest.headers.containsKey("Content-Length")) break
                            bodyStarted = true
                            continue
                        }
                        if (!bodyStarted) with(line.split(": ")) {
                            httpRequest.headers[this[0]] = this[1]
                        }
                        else httpRequest.body += line
                    }
                }
            }
        }
    }

    private class HttpResponse : HttpBase() {
        var statusCode: Int? = null
        var statusText: String? = null
        override fun toString(): String = "$version $statusCode $statusText\n" +
                headers.map { "${it.key}: ${it.value}" }.joinToString("\n") +
                "\n\n" +
                (body ?: "")
    }
}