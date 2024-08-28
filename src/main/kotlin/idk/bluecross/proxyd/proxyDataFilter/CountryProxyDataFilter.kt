package idk.bluecross.proxyd.proxyDataFilter

import idk.bluecross.proxyd.store.IProxyDataFilter
import idk.bluecross.proxyd.store.ProxyData
import idk.bluecross.proxyd.util.ProxyDataMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import java.net.URL
import java.util.Optional

@Component
class CountryProxyDataFilter(
    val proxyDataMapper: ProxyDataMapper
) : IProxyDataFilter {
    @Value("\${filter.country.disallowed:}")
    private var disallowedCountries = listOf<String>()

    @Value("\${filter.country.allowed:}")
    private var allowedCountries = listOf<String>()

    override fun filter(initialFlux: Flux<ProxyData>): Flux<ProxyData> = initialFlux.filter {
        if (it.countryCode.isEmpty)
            it.countryCode = Optional.of(URL("http://ip-api.com/line/${it.ip}?fields=countryCode").readText().trim())
        if (it.countryCode.isPresent) {
            if (allowedCountries.isNotEmpty()) {
                if (allowedCountries.contains(it.countryCode.get())) return@filter true
                else return@filter false
            }
            if (disallowedCountries.contains(it.countryCode.get()))
                return@filter false
        }
        return@filter true
    }
}