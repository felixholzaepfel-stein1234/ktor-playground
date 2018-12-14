import com.google.gson.*
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.html.respondHtml
import io.ktor.http.*
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.html.*
import io.ktor.gson.*
import io.ktor.response.respond
import java.lang.reflect.Type

import java.time.Instant

data class Person(val name : String,
                  val age : Int)

fun buildPersons() = IntRange(1, 4000).map { Person("someName", it) }

private class InstantTypeConverter : JsonSerializer<Instant>, JsonDeserializer<Instant> {
    override fun serialize(src: Instant, srcType: Type, context: JsonSerializationContext): JsonElement {
        return JsonPrimitive(src.toString())
    }

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, type: Type, context: JsonDeserializationContext): Instant {
        return Instant.parse(json.asString)
    }
}


fun main(args: Array<String>) {
    val server = embeddedServer(Netty, 8080) {
        install(DefaultHeaders)
        install(CallLogging)
        install(ContentNegotiation){
            gson {
                setDateFormat("yyyy-MM-dd'T'HH:mm:ssX")
                registerTypeAdapter(Instant::class.javaObjectType, InstantTypeConverter())
            }
        }
        routing {
            get ("/") {
                call.respondHtml {
                    head {
                        title { + "Hello KTOR" }
                    }
                    body {
                        h1 {
                            + "Hello Monkey"
                        }
                        h2 {
                            + "Felix loves you at time ${Instant.now()}!"
                        }

                    }
                }
            }
            get("/timestamp") {
                call.respond(Instant.now())
            }
            get("/persons") {
                call.respond(buildPersons())
            }
        }
    }
    server.start(wait = true)
}