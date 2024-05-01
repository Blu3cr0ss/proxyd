package idk.bluecross.proxyd.exception.exceptions

class UninitializedProxyDataFilterChainException : AbstractFatalException(
    "ProxyDataFilterChainProviderImpl.filterChain is null. Configure ProxyDataFilterChainProviderImpl, or create your own IProxyDataFilterChainProvider",
    "ProxyDataFilterChainProviderImpl.filterChain is null.",
    "Configure ProxyDataFilterChainProviderImpl, or create your own IProxyDataFilterChainProvider."
)