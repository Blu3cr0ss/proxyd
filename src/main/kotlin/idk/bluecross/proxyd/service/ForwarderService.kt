package idk.bluecross.proxyd.service

import idk.bluecross.proxyd.converter.ProxyDataToProxyConverter
import idk.bluecross.proxyd.forwarder.HttpAndHttpsProxy
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import kotlin.concurrent.thread

@Service
@ConditionalOnProperty("forwarder.http.enabled", havingValue = "true", matchIfMissing = true)
class ForwarderService(
    val proxyProviderService: IProxyProviderService,
    val proxyDataToProxyConverter: ProxyDataToProxyConverter
) {
    @Value("\${forwarder.http.port:8081}")
    var port: Int = 8081

    @EventListener(ApplicationStartedEvent::class)
    fun applicationStarted() {
        thread(name = "HttpsForwarderServer") {
            HttpAndHttpsProxy(
                port, 30000, 5000, 30000,
                proxyProviderService, proxyDataToProxyConverter
            ).listen()
        }
    }
}