import com.sun.net.httpserver.HttpServer
import com.sun.net.httpserver.spi.HttpServerProvider
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.net.InetSocketAddress
import java.net.Socket

fun main() {
    printLocalIpAddress()
    val httpServerProvider: HttpServerProvider = HttpServerProvider.provider()

    val socketAddress = InetSocketAddress(8000)

    val httpServer: HttpServer = httpServerProvider.createHttpServer(socketAddress, 0)
    httpServer.createContext("/test/cache") { httpExchange ->
        val remoteAddress = httpExchange.remoteAddress
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        println("$now ${httpExchange.requestMethod} from $remoteAddress")
        val response = StringBuilder()
            .appendLine(now)
            .append("protocol: ").appendLine(httpExchange.protocol)
            .append("request URI: ").appendLine(httpExchange.requestURI)
            .append("method: ").appendLine(httpExchange.requestMethod)
            .append("local address: ").appendLine(httpExchange.localAddress)
            .append("remote address: ").appendLine(remoteAddress)
            .append("headers:").appendLine()
        httpExchange.requestHeaders.forEach {
            response.append('\t').append(it.key).append(" ").appendLine(it.value)
        }
        val requestBody = httpExchange.requestBody.use {
            it.readAllBytes().decodeToString()
        }
        response.appendLine()
            .appendLine("request body:")
            .appendLine(requestBody)
        httpExchange.responseHeaders["Cache-Control"] = listOf("max-age=60")
        httpExchange.sendResponseHeaders(200, response.length.toLong())
        httpExchange.responseBody.use {
            it.write(response.toString().toByteArray())
        }
    }
    httpServer.start()
    println("the http server is running")
}

private fun printLocalIpAddress() = Socket().use { socket ->
    socket.connect(InetSocketAddress("ya.ru", 80))
    val localIp = socket.localAddress.hostAddress
    println("local IP address: $localIp")
}
