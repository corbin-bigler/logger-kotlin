package com.corbinbigler.logger

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

        launch {
            flow.collect {
                assertEquals(message, it.message)
                cancel()
            }
        }
        launch {
            Logger.debug(message = message, tag = UUID.randomUUID().toString())
        }
    }

    @Test
    fun testTagLevel() = runTest {
        val tag = UUID.randomUUID().toString()
        val message = UUID.randomUUID().toString()

        val flow = Logger.flow(tag = tag, level = LogLevel.ERROR).take(2)
        launch {
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
        launch {
            Logger.debug(message, tag)
            Logger.info(message, tag)
            Logger.error(message, tag)
            Logger.error(message, UUID.randomUUID().toString())
            Logger.fault(message, tag)
        }
    }
}
