package idk.bluecross.proxyd.api

import idk.bluecross.proxyd.entity.ProxyData
import idk.bluecross.proxyd.proxyDataProvider.FromControllerProxyDataProvider
import idk.bluecross.proxyd.service.IProxyProviderService
import idk.bluecross.proxyd.util.ProxyDataMapper
import org.springframework.web.bind.annotation.*
import kotlin.jvm.optionals.getOrNull

@RestController
@RequestMapping("/proxy")
class ProxyController(
    val proxyProviderService: IProxyProviderService,
    val fromControllerProxyDataProvider: FromControllerProxyDataProvider,
    val proxyDataMapper: ProxyDataMapper
) : IProxyController {
    @GetMapping("/getMany", produces = ["text/plain"])
    override fun getMany(@RequestParam(required = false) count: Int?): String =
        proxyProviderService.getProxies()
            .run {
                if (count != null) take(count) else this
            }
            .joinToString("\n", transform = { proxyDataMapper.toProxyString(it) })

    @GetMapping("/getOne")
    override fun getOne(): String =
        proxyProviderService.getProxy().getOrNull()?.let { proxyDataMapper.toProxyString(it) } ?: ""

    @GetMapping("/getManyWithMapper", produces = ["text/plain"])
    fun getManyWithMapper(
        @RequestParam(required = false) count: Int?,
        @RequestParam(required = false) type: Boolean = true,
        @RequestParam(required = false) ip: Boolean = true,
        @RequestParam(required = false) port: Boolean = true,
        @RequestParam(required = false) delay: Boolean = true,
        @RequestParam(required = false) rate: Boolean = true,
        @RequestParam(required = false) countryCode: Boolean = true
    ) = proxyProviderService.getProxies()
        .apply { if (count != null) take(count) }
        .joinToString(
            "\n",
            transform = { proxyDataMapper.toStringWithFields(it, type, ip, port, delay, rate, countryCode) })

    @PutMapping("/check")
    override fun check(proxyData: ProxyData) {
        fromControllerProxyDataProvider.send(proxyData)
    }

    @PutMapping("/checkString")
    override fun checkString(@RequestBody proxies: String) {
        proxies.split("\n").map { proxyDataMapper.fromProxyString(it) }.filterNotNull().forEach {
            fromControllerProxyDataProvider.send(it)
        }
    }
}