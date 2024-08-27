package idk.bluecross.proxyd.forwarder.http


class HttpResponseFrame : HttpBaseFrame() {
    var statusCode: Int? = null
    var statusText: String? = null
    fun responseLine() = "$version $statusCode $statusText"
    override fun toString(): String = "${responseLine()}\r\n" +
            headers.map { "${it.key}: ${it.value}" }.joinToString("\r\n") + "\r\n" + (body ?: "\r\n")
}