package idk.bluecross.proxyd.service

import idk.bluecross.proxyd.proxyDataProvider.RecheckProxyDataProvider
import idk.bluecross.proxyd.store.ProxyData
import idk.bluecross.proxyd.store.ProxyDataFilterChain
import idk.bluecross.proxyd.store.IProxyDataProvider
import idk.bluecross.proxyd.store.IProxyProviderService
import idk.bluecross.proxyd.util.ProxyDataMapper
import idk.bluecross.proxyd.util.ValidProxySetHolder
import idk.bluecross.proxyd.util.getLogger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import reactor.core.Disposable
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers
import java.util.*
import java.util.stream.Collectors
import kotlin.concurrent.thread
import kotlin.jvm.optionals.getOrDefault
import kotlin.jvm.optionals.getOrNull
import kotlin.math.log
import kotlin.random.Random

@Service
class ProxyProviderService(
    @Autowired
    val proxyDataProviders: List<IProxyDataProvider>,
    @Autowired
    @Qualifier("verifyProxyDataFilterChain")
    val verifyProxyDataFilterChainProvider: ProxyDataFilterChain,
    @Autowired
    @Qualifier("provideProxyDataFilterChain")
    val provideProxyDataFilterChainProvider: ProxyDataFilterChain,
    val validProxySetHolder: ValidProxySetHolder,
    val proxyDataMapper: ProxyDataMapper,
    val recheckProxyDataProvider: RecheckProxyDataProvider
) : IProxyProviderService {

    private val logger = getLogger()
    private val RANDOM = Random(System.currentTimeMillis())

    override fun getProxies(): Collection<ProxyData> = validProxySetHolder.proxies

    override fun getProxy(): Optional<ProxyData> =
        if (validProxySetHolder.proxies.isEmpty()) Optional.empty() else Optional.of(
            validProxySetHolder.proxies.random(
                RANDOM
            )
        )

    override fun getHttpProxies() = getProxies().filter { it.type == ProxyData.Type.HTTP }
    override fun getSocksProxies() = getProxies().filter { it.type != ProxyData.Type.HTTP }
    override fun getHttpProxy() =
        runCatching { Optional.of(getHttpProxies().random(RANDOM)) }.getOrDefault(Optional.empty())

    override fun getSocksProxy() =
        runCatching { Optional.of(getSocksProxies().random(RANDOM)) }.getOrDefault(Optional.empty())

    /**
     * Сheck the proxies in the set for compliance every 600000ms (10min)
     */
//    @Scheduled(initialDelay = 600000, fixedDelay = 600000, scheduler = "proxyProviderServiceScheduler")
    private fun verifyProxies() {
        if (validProxySetHolder.proxies.isEmpty()) return
        logger.debug("verifyProxies()")
        logger.debug("Before: " + validProxySetHolder.proxies.size)

        var flux = Flux.fromIterable(validProxySetHolder.proxies)

        verifyProxyDataFilterChainProvider.forEach { filter ->
            flux = filter.filter(flux)
                .limitRate(1)
        }

        flux
            .limitRate(Int.MAX_VALUE)
            .collect(Collectors.toSet())
            .block()!!.also { set ->
                validProxySetHolder.proxies.clear()
                validProxySetHolder.proxies.addAll(set)
            }

        logger.debug("After: " + validProxySetHolder.proxies.size)
    }

    private var fluxSubscription: Disposable? = null
    private var mergeProvidersScheduler = Schedulers.newParallel("merge", proxyDataProviders.size)
    private var provideProxiesScheduler = Schedulers.newSingle("provideProxies")

    /**
     * Collect and filter proxies from all ProxyProviders every 900000ms (15 min)
     * @see idk.bluecross.proxyd.proxyDataProvider.IProxyDataProvider
     */
    @Scheduled(initialDelay = 0, fixedRate = 900000, scheduler = "proxyProviderServiceScheduler")
    private fun provideProxies() {
        fluxSubscription?.dispose() // stop previous flux
        logger.debug("provideProxies()")
        var flux = Flux.mergePriority(
            1,
            compareByDescending { it.providedBy.getOrNull()?.priority },
            *proxyDataProviders.map { provider ->
                runCatching {
                    provider.provide()
                        .doOnSubscribe {
                            logger.debug(provider.javaClass.simpleName + " started.")
                        }
                        .doOnComplete {
                            logger.debug(provider.javaClass.simpleName + " stopped.")
                        }
                        .limitRate(Int.MAX_VALUE)
                        .subscribeOn(mergeProvidersScheduler)
                }.onFailure {
                    it.printStackTrace()
                }.getOrElse { Flux.empty() }
            }.toTypedArray()
        )

        provideProxyDataFilterChainProvider.forEach { f ->
            flux = f.filter(flux)
                .limitRate(1)
        }

        flux = flux.doOnNext { proxyData ->
            validProxySetHolder.proxies.add(proxyData)
            logger.info("VALID: " + proxyDataMapper.toProxyString(proxyData))
        }

        fluxSubscription = flux
            .limitRate(Int.MAX_VALUE)
            .subscribeOn(provideProxiesScheduler).subscribe()
    }
}