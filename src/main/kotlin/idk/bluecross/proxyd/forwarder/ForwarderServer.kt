package idk.bluecross.proxyd.forwarder

import idk.bluecross.proxyd.store.IProxyProviderService
import idk.bluecross.proxyd.util.getLogger
import java.net.ServerSocket
import java.util.concurrent.Executors

class ForwarderServer(
    val port: Int,
    private val clientSocketTimeout: Int?,
    private val remoteConnectTimeout: Int,
    private val remoteSocketTimeout: Int?,
    private val proxyProviderService: IProxyProviderService,
) {

    private val serverSocket = ServerSocket(port)

    private val scheduler = Executors.newCachedThreadPool()

    fun listen() {
        getLogger().info("Listening port $port")
        while (true) {
            try {
                scheduler.submit(
                    RequestHandler(
                        serverSocket.accept(),
                        clientSocketTimeout,
                        remoteConnectTimeout,
                        remoteSocketTimeout,
                        proxyProviderService
                    )
                )
            } catch (e: Exception) {
                System.err.println("Unhandled exception: ")
                e.printStackTrace(System.err)
            }
        }
    }
}