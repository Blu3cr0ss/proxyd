package idk.bluecross.proxyd.proxyDataProvider

import idk.bluecross.proxyd.entity.ProxyData
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

@Component
class LocalResourcesProxyDataProvider : IProxyDataProvider {
    override fun provide(): Flux<ProxyData> =
        Flux.fromIterable(
            String(ClassPathResource("proxies.local").inputStream.readAllBytes()).trim().split("\n").flatMap {
                val type = when {
                    it.startsWith("SOCKS4") -> ProxyData.Type.SOCKS4
                    it.startsWith("SOCKS5") -> ProxyData.Type.SOCKS5
                    it.startsWith("SOCKS") -> ProxyData.Type.SOCKS
                    it.startsWith("HTTP") -> ProxyData.Type.HTTP
                    else -> return@flatMap emptyList()
                }
                val split = it.substring(type.name.length + 1).split(":")
                if (split[1] == "*") {
                    val arr = arrayOfNulls<ProxyData>(65536)
                    for (i in 1..65536) {
                        arr[i - 1] = ProxyData(split[0], i, type)
                    }
                    return@flatMap arr.asIterable()
                }
                return@flatMap listOf(ProxyData(split[0], split[1].toInt(), type))
            })
}