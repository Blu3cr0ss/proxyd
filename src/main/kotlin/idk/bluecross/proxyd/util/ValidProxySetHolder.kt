package idk.bluecross.proxyd.util

import idk.bluecross.proxyd.proxyDataProvider.RecheckProxyDataProvider
import idk.bluecross.proxyd.store.ProxyData
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import kotlin.collections.HashSet
import kotlin.concurrent.thread
import kotlin.jvm.optionals.getOrDefault

@Component
class ValidProxySetHolder(val recheckProxyDataProvider: RecheckProxyDataProvider) {
    @Value("\${recheck.timer.s:300}")
    val recheckTimer = 300
    inner class CustomSet : HashSet<ProxyData>() {
        val logger = getLogger()
        override fun add(element: ProxyData): Boolean {
            logger.debug("Adding $element")
            return super.add(element)
        }

        override fun remove(element: ProxyData): Boolean {
            logger.debug("Removing $element")
            return super.remove(element)
        }
        init {
            thread(name = "recheck") {
                while (true) {
                    this.iterator().forEachRemaining {
                        if (System.currentTimeMillis() - it.checkedAt.getOrDefault(0) > recheckTimer * 1000) {
                            this.remove(it)
                            recheckProxyDataProvider.recheck(it)
                        }
                    }
                    Thread.sleep(1000)
                }
            }
        }
    }

    final var proxies = Collections.synchronizedSet(CustomSet())
        private set
}