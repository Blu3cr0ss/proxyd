package idk.bluecross.proxyd.proxyDataProvider

import idk.bluecross.proxyd.entity.ProxyData
import reactor.core.publisher.Flux
import java.net.URL
import java.util.*

/**
 * Collects proxies from https://github.com/TheSpeedX/PROXY-List
 */
class Github_TheSpeedX_PROXY_List_ProxyDataProvider(override var priority: Int = 100) : IProxyDataProvider {
    private val httpList = "https://raw.githubusercontent.com/TheSpeedX/PROXY-List/master/http.txt"
    private val socks5List = "https://raw.githubusercontent.com/TheSpeedX/PROXY-List/master/socks5.txt"

    override fun provide(): Flux<ProxyData> {
        val flux: Flux<ProxyData> = Flux.merge(
            1,
            Flux.fromIterable(URL(httpList).readText().trim().split("\n").map { str ->
                val split = str.split(":")
                ProxyData(ProxyData.Type.HTTP, split[0], split[1].toInt()).apply {
                    providedBy = Optional.of(this@Github_TheSpeedX_PROXY_List_ProxyDataProvider)
                }
            }),
            Flux.fromIterable(URL(socks5List).readText().trim().split("\n").map { str ->
                val split = str.split(":")
                ProxyData(ProxyData.Type.SOCKS, split[0], split[1].toInt()).apply {
                    providedBy = Optional.of(this@Github_TheSpeedX_PROXY_List_ProxyDataProvider)
                }
            })
        )
        return flux
    }
}