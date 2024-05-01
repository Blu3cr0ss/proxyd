package idk.bluecross.proxyd.proxyDataProvider

import idk.bluecross.proxyd.entity.ProxyData
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

@Component
class LocalResourcesProxyDataProvider : IProxyDataProvider {
    override fun provide(): Flux<ProxyData> =
        Flux.fromIterable(String(ClassPathResource("proxies.local").inputStream.readAllBytes()).trim().split("\n").map {
            val split = it.split(":")
            ProxyData(split[0], split[1].toInt(), ProxyData.Type.HTTP)
        })
}