package idk.bluecross.proxyd.proxyDataFilter

import idk.bluecross.proxyd.store.IProxyDataFilter
import idk.bluecross.proxyd.store.ProxyData
import idk.bluecross.proxyd.util.ValidProxySetHolder
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

@Component
class AlreadyValidProxyDataFilter(
    val validProxySetHolder: ValidProxySetHolder
) : IProxyDataFilter {
    override fun filter(initialFlux: Flux<ProxyData>) = initialFlux.filter {
        !validProxySetHolder.proxies.contains(it)
    }
}