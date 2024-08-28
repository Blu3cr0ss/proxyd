package idk.bluecross.proxyd.store

import java.util.Optional

interface IProxyProviderService {
    fun getProxies(): Collection<ProxyData>
    fun getProxy(): Optional<ProxyData>
    fun getHttpProxies(): Collection<ProxyData>
    fun getSocksProxies(): Collection<ProxyData>
    fun getHttpProxy(): Optional<ProxyData>
    fun getSocksProxy(): Optional<ProxyData>
}