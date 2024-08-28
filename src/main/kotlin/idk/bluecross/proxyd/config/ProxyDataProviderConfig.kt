package idk.bluecross.proxyd.config

import idk.bluecross.proxyd.proxyDataProvider.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ProxyDataProviderConfig {
    @Bean
    fun localResourcesProxyDataProvider() = LocalResourcesProxyDataProvider(200)

    @Bean
    fun controllerProxyDataProvider() = ControllerProxyDataProvider(1000)

    @Bean
    fun github_TheSpeedX_PROXY_List_ProxyDataProvider() = Github_TheSpeedX_PROXY_List_ProxyDataProvider()

    @Bean
    fun proxylist_geonode_com_ProxyDataProvider() = Proxylist_geonode_com_ProxyDataProvider()

    @Bean
    fun socks5_provider() = Free_proxy_world_SOCKS5_ProxyDataProvider(199)
    @Bean
    fun recheck_provider() = RecheckProxyDataProvider()
}