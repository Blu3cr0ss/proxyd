package idk.bluecross.proxyd.config

import idk.bluecross.proxyd.entity.ProxyDataFilterChain
import idk.bluecross.proxyd.proxyDataFilter.AlreadyValidProxyDataFilter
import idk.bluecross.proxyd.proxyDataFilter.CountryProxyDataFilter
import idk.bluecross.proxyd.proxyDataFilter.DelayProxyDataFilter
import idk.bluecross.proxyd.proxyDataFilter.RateProxyDataFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ProxyDataFilterChainConfig {
    @Bean("provideProxyDataFilterChain")
    fun provideProxyDataFilterChain(
        alreadyValidProxyDataFilter: AlreadyValidProxyDataFilter,
        delayProxyDataFilter: DelayProxyDataFilter,
        rateProxyDataFilter: RateProxyDataFilter,
        countryProxyDataFilter: CountryProxyDataFilter,
    ) = ProxyDataFilterChain().apply {
        addAll(
            linkedSetOf(
                alreadyValidProxyDataFilter,
                delayProxyDataFilter,
                rateProxyDataFilter,
                countryProxyDataFilter
            )
        )
    }
    @Bean("verifyProxyDataFilterChain")
    fun verifyProxyDataFilterChain(
        alreadyValidProxyDataFilter: AlreadyValidProxyDataFilter,
        delayProxyDataFilter: DelayProxyDataFilter,
        rateProxyDataFilter: RateProxyDataFilter,
        countryProxyDataFilter: CountryProxyDataFilter
    ) = ProxyDataFilterChain().apply {
        addAll(
            linkedSetOf(
                delayProxyDataFilter,
                rateProxyDataFilter,
            )
        )
    }
}