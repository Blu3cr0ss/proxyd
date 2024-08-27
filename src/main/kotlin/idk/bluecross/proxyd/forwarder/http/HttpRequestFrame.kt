package idk.bluecross.proxyd.forwarder.http

import java.io.InputStream

class HttpRequestFrame : HttpBaseFrame() {
    var method: String? = null
    var target: String? = null
    fun url(): String? {
        return if (
            target?.startsWith("http://") == true ||
            target?.startsWith("https://") == true ||
            target?.startsWith("www.") == true
        ) {
            target
        } else "http://$target"
    }

    fun requestLine() = "$method $target $version"

    override fun toString(): String = "${requestLine()}\r\n" +
            headers.map { "${it.key}: ${it.value}" }.joinToString("\r\n") + "\r\n" + (body ?: "\r\n")

    companion object {
        fun parseRequest(inputStream: InputStream): HttpRequestFrame {
            val br = inputStream.bufferedReader()
            val httpRequest = HttpRequestFrame()

            br.readLine()?.split(" ", limit = 3)?.apply {
                httpRequest.method = this[0]
                httpRequest.target = this[1]
                httpRequest.version = this[2]
            } ?: Thread.currentThread().stop()

            var bodyStarted = false
            var line: String? = null
            while (br.readLine()?.also { line = it } != null) {
                if (line!!.isBlank()) {
                    if (!httpRequest.headers.containsKey("Content-Length")) break
                    bodyStarted = true
                    continue
                }
                if (!bodyStarted) with(line!!.split(": ")) {
                    httpRequest.headers[this[0]] = this[1]
                }
                else httpRequest.body += line
            }
            return httpRequest
        }

        fun parseRequest(str: String) {
            val httpRequest = HttpRequestFrame()
            with(str.lines()) {
                val iterator = iterator()
                with(iterator.next().split(" ", limit = 3)) {
                    httpRequest.method = this[0]
                    httpRequest.target = this[1]
                    httpRequest.version = this[2]
                }
                var bodyStarted = false
                while (iterator.hasNext()) {
                    val line = iterator.next()
                    if (line.isBlank()) {
                        if (!httpRequest.headers.containsKey("Content-Length")) break
                        bodyStarted = true
                        continue
                    }
                    if (!bodyStarted) with(line.split(": ")) {
                        httpRequest.headers[this[0]] = this[1]
                    }
                    else httpRequest.body += line
                }
            }
        }
    }
}
