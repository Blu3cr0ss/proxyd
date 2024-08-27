package idk.bluecross.proxyd.proxyDataProvider

import idk.bluecross.proxyd.entity.ProxyData
import org.jsoup.Jsoup
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono
import java.net.URL
import java.util.*
import java.util.concurrent.CompletableFuture

class Free_proxy_world_SOCKS5_ProxyDataProvider(override var priority: Int) : IProxyDataProvider {
    private var page = 1
    private fun getIp(): String =
        "https://www.freeproxy.world/?type=socks5&anonymity=&country=&speed=&port=&page=${page++}"

    override fun provide() = Flux.create { sink ->
        var done = false
        do {
            with(
                Jsoup.connect(getIp()).get()
            ) {
                val ips = select(".layui-table tbody tr td:nth-child(1)").eachText()
                val ports = select(".layui-table tbody tr td:nth-child(2)").eachText().map(String::toInt)
                if (ips.isEmpty() || ports.isEmpty()) done = true
                ips.zip(ports).map {
                    ProxyData(ProxyData.Type.SOCKS5, it.first, it.second).apply {
                        providedBy = Optional.of(this@Free_proxy_world_SOCKS5_ProxyDataProvider)
                    }
                }.forEach { sink.next(it) }
            }
        } while (!done)
        sink.complete()
    }
}