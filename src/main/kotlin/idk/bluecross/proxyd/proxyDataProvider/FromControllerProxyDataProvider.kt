package idk.bluecross.proxyd.proxyDataProvider

import idk.bluecross.proxyd.entity.ProxyData
import idk.bluecross.proxyd.util.getLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import java.net.InetAddress

@Component
class FromControllerProxyDataProvider : IProxyDataProvider {
    private val coroutineContext = newSingleThreadContext("FromControllerProxyDataProvider")
    private val logger = getLogger()
    private val flux = Flux.create<ProxyData> {
        sink = it
    }
    private lateinit var sink: FluxSink<ProxyData>

    fun send(proxyData: ProxyData) {
        CoroutineScope(coroutineContext).launch {
            if (
                (proxyData.port in 1..65536) &&
                runCatching { InetAddress.getByName(proxyData.ip) }.isSuccess
            ) {
                sink.next(proxyData)
            } else if (logger.isWarnEnabled) logger.warn("Invalid ProxyData given: $proxyData")
        }
    }

    override fun provide(): Flux<ProxyData> = flux
}