package dev.limebeck.utils

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

fun Throwable.asHtml(): String = """
    <html>
        <head>
            <link href="https://cdn.jsdelivr.net/npm/water.css@2/out/water.css" rel="stylesheet"/>
            <title>Rendering Error</title>
        </head>
        <body>
            <h1>ERROR</h1>
            <h3>${message ?: this.toString()}</h3>
            <p>Additional error info</p>
            <pre>
                <code>
                    ${stackTraceToString()}
                </code>
            </pre>
        </body>
    </html>
""".trimIndent()

fun String.appendSseReloadScript() = this + """
        <script type="text/javascript">
            var source = new EventSource('/sse');
            source.addEventListener('PageUpdated', function(e) {
                document.documentElement.innerHTML = e.data
            }, false);
        </script>
""".trimIndent()

@OptIn(ExperimentalTime::class)
fun tickerFlow(period: Duration, initialDelay: Duration = Duration.ZERO) = flow {
    delay(initialDelay)
    while (true) {
        emit(Unit)
        delay(period)
    }
}
