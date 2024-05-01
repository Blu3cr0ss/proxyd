package idk.bluecross.proxyd.exception.exceptions

/**
 * This type of exception must stop application from running
 */
open class AbstractFatalException(
    override val message: String?,
    val description: String = "No description provided",
    val action: String = "No action provided"
) : Exception(message)