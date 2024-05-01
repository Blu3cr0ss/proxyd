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
    @Value("\${proxy.maxDelay}")
    private val maxDelay = 200

    @Value("\${proxy.maxDelay}")
    private var pingTimeout = 500
    private val logger = getLogger()

    override fun filter(initialFlux: Flux<ProxyData>) =
        initialFlux
            .parallel(8)
            .runOn(Schedulers.parallel())
            .filter { proxyData ->
                val str = proxyDataMapper.toProxyString(proxyData)
                return@filter runCatching {
                    if (proxyData.delay.isPresent) {
                        if (proxyData.delay.get() > maxDelay) return@filter false
                    } else with(getDelay(proxyData)) {
                        if (this > maxDelay)
                            return@filter false
                        proxyData.delay = Optional.of(this)
                    }
                    return@filter true
                }.onFailure { if (logger.isTraceEnabled) logger.trace("$str | EX | ${it::class.simpleName}: ${it.message}") }.isSuccess
            }
            .sequential()

    private fun getDelay(proxyData: ProxyData): Int {
        val timer = System.currentTimeMillis()
        if (!InetSocketAddress(proxyData.ip, proxyData.port).address.isReachable(pingTimeout))
            throw DestinationUnreachable()
        return (System.currentTimeMillis() - timer).toInt()
    }
}