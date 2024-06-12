package idk.bluecross.proxyd.config

import idk.bluecross.proxyd.proxyDataProvider.ControllerProxyDataProvider
import idk.bluecross.proxyd.proxyDataProvider.Github_TheSpeedX_PROXY_List_ProxyDataProvider
import idk.bluecross.proxyd.proxyDataProvider.LocalResourcesProxyDataProvider
import idk.bluecross.proxyd.proxyDataProvider.Proxylist_geonode_com_ProxyDataProvider
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
}