package idk.bluecross.proxyd.util

import idk.bluecross.proxyd.entity.ProxyData
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.util.*

@Component
class ValidProxySetHolder {
    final var proxies = Collections.synchronizedSet(HashSet<ProxyData>())
        private set
}