import com.benasher44.uuid.uuid4
import dev.limebeck.sse.utils.SseEvent
import dev.limebeck.sse.utils.respondSse
import dev.limebeck.utils.appendSseReloadScript
import dev.limebeck.utils.asHtml
import dev.limebeck.utils.tickerFlow
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

fun main() {
    // FIXME: This is workaround
//    signal(SIGPIPE, staticCFunction<Int, Unit> {
//        println("Interrupt: $it")
//    })
    embeddedServer(CIO, port = 8080) {
        install(StatusPages) {
            exception<Throwable> { call, cause ->
                call.respondText(
                    text = cause.asHtml(),
                    contentType = ContentType.Text.Html,
                    status = HttpStatusCode.InternalServerError
                )
            }
        }
        routing {
            get("/") {
                call.respondText("Initial value".appendSseReloadScript(), contentType = ContentType.Text.Html)
            }

            get("/sse") {
                val updatedStateFlow = MutableStateFlow<String>("Initial")
                launch {
                    val ticker = tickerFlow(1.seconds)
                    ticker.collect {
                        val uuid = uuid4().toString()
                        println("Produced $uuid")
                        try {
                            updatedStateFlow.emit(uuid)
                        } catch (e: Throwable) {
                            updatedStateFlow.emit(e.asHtml())
                        }
                    }
                }
                val counter = atomic(1)
                val events = updatedStateFlow
                    .drop(1)
                    .map {
                        println("Got event $it")
                        SseEvent(
                            data = counter.getAndIncrement().toString(),
                            event = "PageUpdated",
                            id = it
                        )
                    }
                    .produceIn(this)

                call.respondSse(events)
            }
        }
    }.start(wait = true).addShutdownHook {
        println("Shutting down")
    }
}