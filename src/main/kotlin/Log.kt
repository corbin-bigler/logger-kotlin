package com.thysmesi.logger

import java.time.Instant

data class Log(
    val instant: Instant = Instant.now(),
    val tag: String,
    val message: String,
    val level: LogLevel,
    val secure: Boolean,
    val metadata: Map<String, Any>
)