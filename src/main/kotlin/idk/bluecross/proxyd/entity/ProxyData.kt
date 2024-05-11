package idk.bluecross.proxyd.entity

import java.util.Objects
import java.util.Optional

data class ProxyData(val ip: String, val port: Int, val type: Type) {
    var delay = Optional.empty<Int>()
    var rate = Optional.empty<Int>()
    var countryCode = Optional.empty<String>()
    var anonymity = Optional.empty<Anonymity>()

    enum class Anonymity {
        TRANSPARENT, ANONYMOUS, ELITE
    }

    enum class Type {
        HTTP, SOCKS, SOCKS4, SOCKS5
    }


    override fun equals(other: Any?): Boolean {
        if (other !is ProxyData) return false
        return other.ip == this.ip && other.port == this.port && other.type == this.type
    }

    override fun hashCode(): Int = Objects.hash(ip, port, type)
}