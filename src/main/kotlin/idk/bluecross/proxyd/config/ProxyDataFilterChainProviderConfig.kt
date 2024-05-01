package idk.bluecross.proxyd.config

import idk.bluecross.proxyd.entity.ProxyDataFilterChain
import idk.bluecross.proxyd.proxyDataFilter.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ProxyDataFilterChainProviderConfig {
    @Bean
    fun proxyDataFilterChainProvider(
        alreadyValidProxyDataFilter: AlreadyValidProxyDataFilter,
        delayProxyDataFilter: DelayProxyDataFilter,
        rateProxyDataFilter: RateProxyDataFilter,
        countryProxyDataFilter: CountryProxyDataFilter
    ) = ProxyDataFilterChainProvider().apply {
        val chain = ProxyDataFilterChain()
        chain.addAll(
            linkedSetOf(
                alreadyValidProxyDataFilter,
                delayProxyDataFilter,
                rateProxyDataFilter,
                countryProxyDataFilter
            )
        )
        setFilterChain(chain)
    }
}