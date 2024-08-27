package idk.bluecross.proxyd.forwarder.http

abstract class HttpBaseFrame {
    var version = "HTTP/1.1"
    var headers = hashMapOf<String, String>()
    var body: String? = null
}