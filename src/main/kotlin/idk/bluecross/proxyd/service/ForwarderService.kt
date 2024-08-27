package idk.bluecross.proxyd.service

import idk.bluecross.proxyd.converter.ProxyDataToProxyConverter
import idk.bluecross.proxyd.forwarder.ForwarderServer
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import kotlin.concurrent.thread

@Service
@ConditionalOnProperty("forwarder.enabled", havingValue = "true", matchIfMissing = true)
class ForwarderService(
    val proxyProviderService: IProxyProviderService,
) {
    @Value("\${forwarder.port:8081}")
    var port: Int = 8081

    @Value("\${forwarder.clientSocketTimeout:5000}")
    var clientSocketTimeout: Int = 5000

    @Value("\${forwarder.serverSocketTimeout:8081}")
    var serverSocketTimeout: Int = 5000

    @Value("\${forwarder.serverConnectTimeout:8081}")
    var serverConnectTimeout: Int = 5000

    @EventListener(ApplicationStartedEvent::class)
    fun applicationStarted() {
        thread(name = "HttpsForwarderServer") {
            ForwarderServer(
                port, clientSocketTimeout, serverSocketTimeout, serverConnectTimeout,
                proxyProviderService
            ).listen()
        }
    }
}