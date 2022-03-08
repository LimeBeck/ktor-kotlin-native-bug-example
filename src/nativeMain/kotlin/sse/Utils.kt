package dev.limebeck.sse.utils

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.utils.io.*
import io.ktor.utils.io.errors.*
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope


data class SseEvent(val data: String, val event: String? = null, val id: String? = null)

suspend fun ApplicationCall.respondSse(events: ReceiveChannel<SseEvent>) =
    coroutineScope {
        response.cacheControl(CacheControl.NoCache(null))
        respondBytesWriter(contentType = ContentType.Text.EventStream) {
            events.consumeEach { event ->
                if (isClosedForWrite) {
                    println("<b785289> Closed socked. Cancel execution")
                    this@coroutineScope.cancel()
                } else {
                    println("<0c08a270> Send event $event")
                    try {
                        if (event.id != null) {
                            writeStringUtf8("id: ${event.id}\n")
                        }
                        if (event.event != null) {
                            writeStringUtf8("event: ${event.event}\n")
                        }
                        for (dataLine in event.data.lines()) {
                            writeStringUtf8("data: $dataLine\n")
                        }
                        writeStringUtf8("\n")
                        flush()
                    } catch (e: IOException) {
                        println("<b785289> Closed socked. Cancel execution")
                        this@coroutineScope.cancel()
                    }
                }
            }
        }
    }
