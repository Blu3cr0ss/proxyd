package idk.bluecross.proxyd.proxyDataProvider

import idk.bluecross.proxyd.entity.ProxyData
import reactor.core.publisher.Flux

interface IProxyDataProvider {
    fun provide(): Flux<ProxyData>
}