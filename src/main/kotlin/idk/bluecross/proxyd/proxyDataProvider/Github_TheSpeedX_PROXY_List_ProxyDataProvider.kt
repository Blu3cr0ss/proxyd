package idk.bluecross.proxyd.proxyDataProvider

import idk.bluecross.proxyd.entity.ProxyData
import idk.bluecross.proxyd.service.ProxyStatsService
import kotlinx.coroutines.*
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers
import java.net.URL
import java.util.concurrent.atomic.AtomicLong

/**
 * Collects proxies from https://github.com/TheSpeedX/PROXY-List
 */
@Component
class Github_TheSpeedX_PROXY_List_ProxyDataProvider(val proxyStatsService: ProxyStatsService) : IProxyDataProvider {
    private val httpList = "https://raw.githubusercontent.com/TheSpeedX/PROXY-List/master/http.txt"
    private val socks5List = "https://raw.githubusercontent.com/TheSpeedX/PROXY-List/master/socks5.txt"

    override fun provide(): Flux<ProxyData> {
        var flux: Flux<ProxyData> = Flux.merge(
            Flux.fromIterable(URL(httpList).readText().trim().split("\n").map { str ->
                val split = str.split(":")
                ProxyData(split[0], split[1].toInt(), ProxyData.Type.HTTP)
            }),
            Flux.fromIterable(URL(socks5List).readText().trim().split("\n").map { str ->
                val split = str.split(":")
                ProxyData(split[0], split[1].toInt(), ProxyData.Type.SOCKS)
            })
        )
        return flux
    }
}