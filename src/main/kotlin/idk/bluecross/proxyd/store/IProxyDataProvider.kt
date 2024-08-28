package idk.bluecross.proxyd.store

import reactor.core.publisher.Flux

interface IProxyDataProvider {
    fun provide(): Flux<ProxyData>
    var priority: Int
}