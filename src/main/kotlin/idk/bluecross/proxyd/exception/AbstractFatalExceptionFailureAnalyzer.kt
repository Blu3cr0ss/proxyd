package idk.bluecross.proxyd.exception

import idk.bluecross.proxyd.exception.exceptions.AbstractFatalException
import idk.bluecross.proxyd.util.getLogger
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.diagnostics.AbstractFailureAnalyzer
import org.springframework.boot.diagnostics.FailureAnalysis
import org.springframework.stereotype.Component
import kotlin.math.log

// TODO сделать что бы это работало
@Component
class AbstractFatalExceptionFailureAnalyzer : AbstractFailureAnalyzer<AbstractFatalException>() {
    private val logger = getLogger()
    init {
        if (logger.isDebugEnabled) logger.debug("AbstractFatalExceptionFailureAnalyzer initialized.")
    }
    override fun analyze(rootFailure: Throwable?, cause: AbstractFatalException?): FailureAnalysis {
        return FailureAnalysis(cause?.description, cause?.action, cause)
    }
}