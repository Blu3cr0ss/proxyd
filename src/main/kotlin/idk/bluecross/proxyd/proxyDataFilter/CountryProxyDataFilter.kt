package idk.bluecross.proxyd.proxyDataFilter

import idk.bluecross.proxyd.entity.ProxyData
import idk.bluecross.proxyd.util.ProxyDataMapper
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import java.net.URL
import java.util.Optional

@Component
class CountryProxyDataFilter(
    val proxyDataMapper: ProxyDataMapper
) : IProxyDataFilter {
    override fun filter(initialFlux: Flux<ProxyData>): Flux<ProxyData> = initialFlux.doOnNext {
        if (it.countryCode.isEmpty)
            it.countryCode = Optional.of(URL("http://ip-api.com/line/24.48.0.1?fields=countryCode").readText())
        proxyDataMapper.toProxyString(it)
    }
}