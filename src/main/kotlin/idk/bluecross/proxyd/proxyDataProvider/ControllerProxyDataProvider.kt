package idk.bluecross.proxyd.proxyDataProvider

import idk.bluecross.proxyd.entity.ProxyData
import idk.bluecross.proxyd.util.getLogger
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import java.net.InetAddress
import java.util.*

class ControllerProxyDataProvider(override var priority: Int = 100) : IProxyDataProvider {
    private val logger = getLogger()
    private val flux = Flux.create {
        sink = it
    }
    private lateinit var sink: FluxSink<ProxyData>

    fun send(proxyData: ProxyData) {
        if (
            (proxyData.port in 1..65536) &&
            runCatching { InetAddress.getByName(proxyData.ip) }.isSuccess
        ) {
            sink.next(proxyData.apply { providedBy = Optional.of(this@ControllerProxyDataProvider) })
        } else logger.warn("Invalid ProxyData given: $proxyData")
    }

    override fun provide(): Flux<ProxyData> = flux
}