package idk.bluecross.proxyd.api

import idk.bluecross.proxyd.store.ProxyData

interface IProxyController {
    fun getMany(count: Int?): String
    fun getOne(): String
    fun check(proxyData: ProxyData)
    fun checkString(proxies: String)
}