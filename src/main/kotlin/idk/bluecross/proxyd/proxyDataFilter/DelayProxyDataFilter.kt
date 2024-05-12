package idk.bluecross.proxyd.proxyDataFilter

import idk.bluecross.proxyd.entity.ProxyData
import idk.bluecross.proxyd.exception.exceptions.DestinationUnreachable
import idk.bluecross.proxyd.util.ProxyDataMapper
import idk.bluecross.proxyd.util.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers
import java.net.InetSocketAddress
import java.util.*

@Component
class DelayProxyDataFilter(
    val proxyDataMapper: ProxyDataMapper,
) : IProxyDataFilter {
    @Value("\${filter.delay.max:200}")
    private var maxDelay = 200

    @Value("\${filter.delay.parallelism:8}")
    private var parallelism = 8

    private val logger = getLogger()

    override fun filter(initialFlux: Flux<ProxyData>) =
        initialFlux
            .parallel(parallelism)
            .runOn(Schedulers.parallel())
            .filter { proxyData ->
                return@filter runCatching {
                    if (proxyData.delay.isPresent) proxyData.delay.get()
                    else getDelay(proxyData)
                }
                    .onFailure { if (it !is DestinationUnreachable) logger.debug(it) }
                    .onSuccess {
                        proxyData.delay = Optional.of(it)
                    }
                    .getOrElse { return@filter false } <= maxDelay
            }
            .sequential(1)

    private fun getDelay(proxyData: ProxyData): Int {
        val timer = System.currentTimeMillis()
        if (!InetSocketAddress(proxyData.ip, proxyData.port).address.isReachable(maxDelay))
            throw DestinationUnreachable()
        return (System.currentTimeMillis() - timer).toInt()
    }
}