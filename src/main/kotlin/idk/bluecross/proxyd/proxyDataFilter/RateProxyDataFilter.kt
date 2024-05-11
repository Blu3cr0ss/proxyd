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
import kotlin.math.min

@Component
class RateProxyDataFilter(
    val proxyDataMapper: ProxyDataMapper,
    val proxyDataToProxyConverter: ProxyDataToProxyConverter,
) : IProxyDataFilter {
    private val logger = getLogger()

    @Value("\${filter.rate.min:1000}")
    private var minRate = 1000

    @Value("\${filter.rate.connectTimeout:500}")
    private var connectTimeout = 500

    @Value("\${filter.rate.readTimeout:500}")
    private var readTimeout = 500

    @Value("\${filter.rate.resource:https://raw.githubusercontent.com/Blu3cr0ss/test-files/main/20M}")
    private var rateResource = "https://raw.githubusercontent.com/Blu3cr0ss/test-files/main/20M"
    @Value("\${filter.rate.parallelism:2}")
    private var parallelism = 2

    override fun filter(initialFlux: Flux<ProxyData>): Flux<ProxyData> = initialFlux
        .parallel(parallelism)
        .runOn(Schedulers.parallel())
        .filter { proxyData ->
            return@filter runCatching {
                if (proxyData.rate.isPresent) proxyData.delay.get()
                else getRate(proxyData)
            }
                .onFailure { if (logger.isTraceEnabled) logger.trace(it) }
                .onSuccess {
                    proxyData.rate = Optional.of(it)
                }
                .getOrElse { return@filter false } >= minRate
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