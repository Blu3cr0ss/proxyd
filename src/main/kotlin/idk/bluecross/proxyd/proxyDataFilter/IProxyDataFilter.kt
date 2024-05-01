package idk.bluecross.proxyd.proxyDataFilter

import idk.bluecross.proxyd.entity.ProxyData
import reactor.core.publisher.Flux

interface IProxyDataFilter {
    fun filter(initialFlux: Flux<ProxyData>): Flux<ProxyData>
}