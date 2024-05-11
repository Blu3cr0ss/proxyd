package idk.bluecross.proxyd.service

import idk.bluecross.proxyd.entity.ProxyData
import idk.bluecross.proxyd.proxyDataFilter.IProxyDataFilterChainProvider
import idk.bluecross.proxyd.proxyDataProvider.IProxyDataProvider
import idk.bluecross.proxyd.util.ProxyDataMapper
import idk.bluecross.proxyd.util.ValidProxySetHolder
import idk.bluecross.proxyd.util.getLogger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import reactor.core.Disposable
import reactor.core.publisher.Flux
import java.util.*
import java.util.stream.Collectors

@Service
class ProxyProviderService(
    @Autowired
    val proxyDataProviders: List<IProxyDataProvider>,
    val proxyDataFilterChainProvider: IProxyDataFilterChainProvider,
    val validProxySetHolder: ValidProxySetHolder,
    val proxyStatsService: ProxyStatsService,
    val proxyDataMapper: ProxyDataMapper
) : IProxyProviderService {
    private val logger = getLogger()

    override fun getProxies(): Collection<ProxyData> = validProxySetHolder.proxies

    override fun getProxy(): Optional<ProxyData> =
        if (validProxySetHolder.proxies.isEmpty()) Optional.empty() else Optional.of(validProxySetHolder.proxies.first())

    /**
     * Сheck the proxies in the set for compliance every 600000ms (10min)
     */
    @Scheduled(fixedDelay = 600000, scheduler = "proxyProviderServiceScheduler")
    private fun verifyProxies() {
        if (validProxySetHolder.proxies.isEmpty()) return
        if (logger.isDebugEnabled) {
            logger.debug("verifyProxies()")
            logger.debug("Before: " + validProxySetHolder.proxies.toString())
        }

        var flux = Flux.fromIterable(validProxySetHolder.proxies)
        proxyDataFilterChainProvider.provide().forEach { filter ->
            flux = filter.filter(flux)
        }
        flux.collect(Collectors.toSet()).block()!!.also { set ->
            validProxySetHolder.proxies.clear()
            validProxySetHolder.proxies.addAll(set)
            proxyStatsService.setValid(set.size.toLong())
        }

        if (logger.isDebugEnabled) logger.debug("After: " + validProxySetHolder.proxies.toString())
    }

    private var fluxSubscription: Disposable? = null

    /**
     * Collect and filter proxies from all ProxyProviders every 1800000ms (30 min)
     * @see idk.bluecross.proxyd.proxyDataProvider.IProxyDataProvider
     */
    @Scheduled(initialDelay = 0, fixedRate = 1800000, scheduler = "proxyProviderServiceScheduler")
    private fun provideProxies() {
        fluxSubscription?.dispose() // stop previous flux
        if (logger.isDebugEnabled) logger.debug("provideProxies()")
        var flux = Flux.fromIterable(proxyDataProviders)
            .flatMap { provider ->
                provider.provide()
            }.doOnNext {
                proxyStatsService.incTotal()
            }

        proxyDataFilterChainProvider.provide().forEach { filter ->
            flux = filter.filter(flux)
        }

        flux = flux.doOnNext { proxyData ->
            if (logger.isInfoEnabled) logger.info("VALID: " + proxyDataMapper.toProxyString(proxyData))
            proxyStatsService.incValid()
            validProxySetHolder.proxies.add(proxyData)
        }

        fluxSubscription = flux.subscribe()
    }
}