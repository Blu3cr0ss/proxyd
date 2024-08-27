package idk.bluecross.proxyd.entity

import idk.bluecross.proxyd.proxyDataProvider.IProxyDataProvider
import java.net.Proxy
import java.util.*

data class ProxyData( val type: Type,val ip: String, val port: Int) {
    var delay = Optional.empty<Int>()
    var rate = Optional.empty<Int>()
    var countryCode = Optional.empty<String>()
    var anonymity = Optional.empty<Anonymity>()
    var providedBy = Optional.empty<IProxyDataProvider>()

    enum class Anonymity {
        TRANSPARENT, ANONYMOUS, ELITE
    }

    enum class Type {
        HTTP, SOCKS, SOCKS4, SOCKS5;
        fun toJavaProxyType(): Proxy.Type {
            return when(this){
                HTTP -> Proxy.Type.HTTP
                else -> Proxy.Type.SOCKS
            }
        }
    }



    override fun equals(other: Any?): Boolean {
        if (other !is ProxyData) return false
        return other.ip == this.ip && other.port == this.port && other.type == this.type
    }

    override fun hashCode(): Int = Objects.hash(ip, port, type)
}