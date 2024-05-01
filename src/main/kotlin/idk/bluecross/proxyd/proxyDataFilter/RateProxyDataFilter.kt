package idk.bluecross.proxyd.proxyDataFilter

import idk.bluecross.proxyd.converter.ProxyDataToProxyConverter
import idk.bluecross.proxyd.entity.ProxyData
import idk.bluecross.proxyd.util.ProxyDataMapper
import idk.bluecross.proxyd.util.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

@Component
class RateProxyDataFilter(
    val proxyDataMapper: ProxyDataMapper,
    val proxyDataToProxyConverter: ProxyDataToProxyConverter,
) : IProxyDataFilter {
    private val logger = getLogger()
    @Value("\${proxy.minRate}")
    private val minRate = 100

    @Value("\${proxy.rate.connectTimeout}")
    private var connectTimeout = 500

    @Value("\${proxy.rate.readTimeout}")
    private var readTimeout = 500

    @Value("\${proxy.rate.resource}")
    private var rateResource = "https://google.com"

    override fun filter(initialFlux: Flux<ProxyData>): Flux<ProxyData> = initialFlux
        .parallel(8)
        .runOn(Schedulers.parallel())
        .filter { proxyData ->
            val str = proxyDataMapper.toProxyString(proxyData)
            return@filter runCatching {
                if (proxyData.rate.isPresent) {
                    if (proxyData.rate.get() < minRate) return@filter false
                } else with(getRate(proxyData)) {
                    if (this < minRate)
                        return@filter false
                    proxyData.rate = Optional.of(this)
                }
            }.onFailure { if (logger.isTraceEnabled) logger.trace("$str | EX | ${it::class.simpleName}: ${it.message}") }.isSuccess
        }
        .sequential()

    /**
     * Gets speed rate to given in properties host
     */
    private fun getRate(proxyData: ProxyData): Int {
        val conn = URL(rateResource).openConnection(proxyDataToProxyConverter.convert(proxyData)) as HttpURLConnection
        conn.connectTimeout = connectTimeout
        conn.readTimeout = readTimeout
        conn.connect()

        val start = System.currentTimeMillis()
        conn.inputStream.readAllBytes()
        val end = System.currentTimeMillis()

        if (logger.isDebugEnabled) logger.debug(
            "${
                proxyDataMapper.toProxyString(
                    proxyData
                )
            } RETURNED ${conn.responseCode} ${conn.responseMessage}. Content-Length=${conn.contentLength}"
        )

        return (conn.contentLength / (end - start)).toInt()     //kb/s
    }
}