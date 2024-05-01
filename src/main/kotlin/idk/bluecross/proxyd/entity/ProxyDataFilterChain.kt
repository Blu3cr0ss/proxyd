package idk.bluecross.proxyd.entity

import idk.bluecross.proxyd.proxyDataFilter.IProxyDataFilter
import java.util.LinkedList

class ProxyDataFilterChain : LinkedList<IProxyDataFilter>()