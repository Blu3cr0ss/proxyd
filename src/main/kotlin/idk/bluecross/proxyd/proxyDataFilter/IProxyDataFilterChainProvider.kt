package idk.bluecross.proxyd.proxyDataFilter

import idk.bluecross.proxyd.entity.ProxyDataFilterChain

interface IProxyDataFilterChainProvider {
    fun provide(): ProxyDataFilterChain
}