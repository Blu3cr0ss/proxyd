package idk.bluecross.proxyd.converter

import idk.bluecross.proxyd.store.ProxyData
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Service
import java.net.InetSocketAddress
import java.net.Proxy

@Service
class ProxyDataToProxyConverter : Converter<ProxyData, Proxy> {
    override fun convert(source: ProxyData): Proxy =
        Proxy(
            when (source.type) {
                ProxyData.Type.SOCKS4, ProxyData.Type.SOCKS5, ProxyData.Type.SOCKS -> Proxy.Type.SOCKS
                ProxyData.Type.HTTP -> Proxy.Type.HTTP
            }, InetSocketAddress(source.ip, source.port)
        )
}