package idk.bluecross.proxyd.proxyDataProvider

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import idk.bluecross.proxyd.entity.ProxyData
import idk.bluecross.proxyd.util.getLogger
import reactor.core.publisher.Flux
import java.net.URL
import java.util.*
import kotlin.math.ceil

/**
 * https://proxylist.geonode.com/api/proxy-list
 */
class Proxylist_geonode_com_ProxyDataProvider(override var priority: Int = 100) : IProxyDataProvider {

    private val url =
        "https://proxylist.geonode.com/api/proxy-list?limit=500&page=#PAGE&sort_by=responseTime&sort_type=asc"
    private val mapper = jacksonObjectMapper()
    private val logger = getLogger()

    @JsonIgnoreProperties(ignoreUnknown = true)
    private class ProxyInfo(
        var ip: String,
        var port: Int,
        var anonymityLevel: String,
        var country: String,
        var protocols: List<String>
    )

    override fun provide(): Flux<ProxyData> {
        val time = System.currentTimeMillis()
        var total = 1
        var pages = 1
        val proxyInfoList = arrayListOf<ProxyInfo>()
        val reader = mapper.readerFor(object : TypeReference<ArrayList<ProxyInfo>>() {})

        var i = 1
        while (i in 1..pages) {
            mapper.readTree(URL(url.replace("#PAGE", i.toString())))
                .also {
                    total = it.get("total").intValue()
                    pages = ceil(total / 500.0).toInt()
                    proxyInfoList.ensureCapacity(total)
                }
                .get("data").also {
                    proxyInfoList.addAll(reader.readValue(it))
                }
            i++
        }
        return Flux.fromIterable(proxyInfoList.map(this::convert))
    }

    private fun convert(proxyInfo: ProxyInfo): ProxyData = ProxyData(
        when (proxyInfo.protocols.first()) {
            "socks4" -> ProxyData.Type.SOCKS4
            "socks5" -> ProxyData.Type.SOCKS5
            "http" -> ProxyData.Type.HTTP
            else -> ProxyData.Type.HTTP
        }, proxyInfo.ip, proxyInfo.port
    ).apply {
        countryCode = Optional.of(proxyInfo.country)
        anonymity = Optional.of(ProxyData.Anonymity.valueOf(proxyInfo.anonymityLevel.uppercase()))
        providedBy = Optional.of(this@Proxylist_geonode_com_ProxyDataProvider)
    }
}