package idk.bluecross.proxyd.util

import org.apache.juli.logging.LogFactory

fun Any.getLogger() = LogFactory.getLog(this::class.java)