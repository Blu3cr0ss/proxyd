package idk.bluecross.proxyd.proxyDataProvider

import idk.bluecross.proxyd.store.IProxyDataProvider
import idk.bluecross.proxyd.store.ProxyData
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink

class RecheckProxyDataProvider : IProxyDataProvider {
    private lateinit var sink: FluxSink<ProxyData>
    private val flux = Flux.create { sink = it }
    fun recheck(proxyData: ProxyData) {
        sink.next(proxyData)
    }

    override fun provide() = flux

    override var priority = Int.MAX_VALUE
}