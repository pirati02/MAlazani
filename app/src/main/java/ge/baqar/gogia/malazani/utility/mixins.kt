package ge.baqar.gogia.malazani.utility

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

inline fun <reified T : Enum<T>> enumFromIndex(i: Int) = enumValues<T>()[i]

@OptIn(ExperimentalTime::class)
fun tickerFlow(period: Duration, initialDelay: Duration = Duration.ZERO) = flow {
    delay(initialDelay)
    while (true) {
        emit(Unit)
        delay(period)
    }
}