package idk.bluecross.proxyd.proxyDataFilter

import idk.bluecross.proxyd.entity.ProxyDataFilterChain
import idk.bluecross.proxyd.exception.exceptions.UninitializedProxyDataFilterChainException
import idk.bluecross.proxyd.util.getLogger
import org.springframework.stereotype.Component
import kotlin.concurrent.thread
import kotlin.system.exitProcess

@Component
class ProxyDataFilterChainProvider : IProxyDataFilterChainProvider {
    private var filterChain: ProxyDataFilterChain? = null
    private val logger = getLogger()

    fun setFilterChain(filterChain: ProxyDataFilterChain) {
        if (this.filterChain != null) logger.warn("Received a request to set filterChain, but filterChain is already set: $filterChain.")
        this.filterChain = filterChain
    }

    override fun provide(): ProxyDataFilterChain {
        if (filterChain == null) {
            thread {
                Thread.sleep(1000)
                exitProcess(1)
            }
            throw UninitializedProxyDataFilterChainException()
        }
        return filterChain!!
    }
}