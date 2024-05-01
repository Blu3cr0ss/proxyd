package idk.bluecross.proxyd.util

import idk.bluecross.proxyd.entity.ProxyData
import idk.bluecross.proxyd.entity.ProxyData.Type
import org.springframework.stereotype.Component
import java.util.StringJoiner
import kotlin.jvm.optionals.getOrNull

@Component
class ProxyDataMapper {
    fun fromProxyString(string: String): ProxyData? = runCatching {
        val type = Type.valueOf(string.split(" ")[0])
        val addr = string.substring(type.name.length + 1).split(":")
        val ip = addr[0]
        val port = addr[1].toInt()
        return@runCatching ProxyData(ip, port, type)
    }.getOrNull()


    fun toProxyString(proxyData: ProxyData) = "${proxyData.type} ${proxyData.ip}:${proxyData.port}"

    fun toStringWithFields(
        proxyData: ProxyData,
        type: Boolean = true,
        ip: Boolean = true,
        port: Boolean = true,
        delay: Boolean = true,
        rate: Boolean = true,
        countryCode: Boolean = true
    ): String {
        val joiner = StringJoiner(" ")
        if (type)
            joiner.add(proxyData.type.toString())
        if (ip && port)
            joiner.add(proxyData.ip + ":" + proxyData.port)
        else if (ip)
            joiner.add(proxyData.ip)
        else if (port)
            joiner.add(proxyData.ip)
        if (delay)
            joiner.add(proxyData.delay.getOrNull()?.toString())
        if (rate)
            joiner.add(proxyData.rate.getOrNull()?.toString())
        if (countryCode)
            joiner.add(proxyData.countryCode.getOrNull()?.toString())
        return joiner.toString()
    }
}