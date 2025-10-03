package com.thysmesi.logger

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

class LoggerTests {
    @Test
    fun testNoTagNoLevel() = runTest {
        val message = UUID.randomUUID().toString()
        val flow = Logger.flow()

        backgroundScope.launch {
            flow.collect {
                assertEquals(message, it.message)
                cancel()
            }
        }

        Logger.debug(message = message, tag = UUID.randomUUID().toString())
    }

    @Test
    fun testTagLevel() = runTest {
        val tag = UUID.randomUUID().toString()
        val message = UUID.randomUUID().toString()

        val flow = Logger.flow(tag = tag, level = LogLevel.ERROR).take(2)
        backgroundScope.launch {
            var count = 0
            println("a")
            flow.collect { log ->
                println("b: $count")
                assertEquals(message, log.message)
                assertEquals(tag, log.tag)
                when (count) {
                    0 -> assertEquals(LogLevel.ERROR, log.level)
                    1 -> {
                        assertEquals(LogLevel.FAULT, log.level)
                        cancel()
                    }
                }
                count++
            }
        }

        Logger.debug(message, tag) // too low level, skipped
        Logger.info(message, tag)  // too low level, skipped
        Logger.error(message, tag) // should be collected
        Logger.error(message, UUID.randomUUID().toString()) // wrong tag, skipped
        Logger.fault(message, tag) // should be collected
    }
}
