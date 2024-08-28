package idk.bluecross.proxyd.store

import reactor.core.publisher.Flux

interface IProxyDataFilter {
    fun filter(initialFlux: Flux<ProxyData>): Flux<ProxyData>
}