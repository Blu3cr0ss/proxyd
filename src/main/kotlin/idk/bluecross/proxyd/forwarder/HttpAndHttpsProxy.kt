package idk.bluecross.proxyd.forwarder

import idk.bluecross.proxyd.converter.ProxyDataToProxyConverter
import idk.bluecross.proxyd.entity.ProxyData
import idk.bluecross.proxyd.service.IProxyProviderService
import idk.bluecross.proxyd.util.getLogger
import reactor.core.scheduler.Schedulers
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.ServerSocket
import java.util.concurrent.atomic.AtomicLong

class HttpAndHttpsProxy(
    val port: Int,
    val clientSocketTimeout: Int?,
    val remoteConnectTimeout: Int,
    val remoteSocketTimeout: Int?,
    val proxyProviderService: IProxyProviderService,
    val proxyDataToProxyConverter: ProxyDataToProxyConverter
) {

    private val serverSocket = ServerSocket(port)

    private val counter = AtomicLong()
    private val scheduler = Schedulers.newBoundedElastic(
        16, Int.MAX_VALUE,
        { runnable ->
            Thread(runnable, "HttpAndHttpsWorker-" + counter.incrementAndGet())
        }, 60
    )

    fun listen() {
        getLogger().info("Listening port $port")
        while (true) {
            try {
                val socket = serverSocket.accept()
                scheduler.schedule(
                    HttpHttpsRequestHandler(
                        socket,
                        clientSocketTimeout,
                        remoteConnectTimeout,
                        remoteSocketTimeout,
                        proxyProviderService.getProxies()
                            .filter { it.type == ProxyData.Type.HTTP }
                            .takeIf { it.isNotEmpty() }
                            ?.random()
                            ?.run(proxyDataToProxyConverter::convert)
                    )
                )
            } catch (e: Exception) {
                System.err.println("Unhandled exception: ")
                e.printStackTrace(System.err)
            }
        }
    }
}