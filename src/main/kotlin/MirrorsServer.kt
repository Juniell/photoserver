import fragments.photoChooser.DIR_MINIS
import io.ktor.http.*
import kotlinx.serialization.json.Json
import io.ktor.http.content.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import java.io.File


object MirrorsServer {
    private const val port = 8888
    private var isWorks = false
    private lateinit var server: NettyApplicationEngine

    fun start() {
        server = embeddedServer(Netty, port = port, host = "0.0.0.0") {
            installJson()
            configureRouting()
        }
        server.start()
        isWorks = true
    }

    fun stop() {
        server.stop()
        isWorks = false
    }

    fun getPort() = port

    fun isWorks() = isWorks

    private fun Application.installJson() {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }
    }

    private fun Application.configureRouting() {
        routing {
            get("/mirror/check_connect") {
                call.response.status(HttpStatusCode.OK)
            }

            post("/mirror/photo") {
                val multipartData = call.receiveMultipart()

                multipartData.forEachPart { part ->
                    if (part is PartData.FileItem) {
                        val fileName = part.originalFileName as String
                        if (isImageFile(fileName)) {
                            val fileBytes = part.streamProvider().readBytes()
                            val file = File(Settings.dirInput + File.separator + fileName)

                            file.writeBytes(fileBytes)
                            createMini(file, Settings.dirInput + File.separator + DIR_MINIS)
                        }
                    }
                }
                call.response.status(HttpStatusCode.OK)
            }
        }
    }
}