package com.thysmesi.logger

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter

class Logger private constructor(
    val defaultTag: String?,
    private val logFlow: MutableSharedFlow<Log>
) {
    constructor(defaultTag: String? = null): this(
        defaultTag = defaultTag,
        logFlow = MutableSharedFlow(
            replay = 0,
            extraBufferCapacity = Int.MAX_VALUE,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
    )

    fun tagged(tag: String): Logger { return Logger(tag, logFlow) }

    fun flow(tag: String? = null, level: LogLevel = LogLevel.DEBUG): Flow<Log> {
        return logFlow.filter { log ->
            (tag?.let { it == log.tag } ?: true) && log.level.value <= level.value
        }
    }

    fun log(message: Any?, tag: String, level: LogLevel, secure: Boolean = false, metadata: Map<String, Any> = mapOf()) {
        logFlow.tryEmit(
            Log(
                tag = tag,
                message = "$message",
                level = level,
                secure = secure,
                metadata = metadata,
            )
        )
    }

    private fun logWithoutMetadata(
        message: Any?,
        tag: String? = defaultTag,
        level: LogLevel,
        secure: Boolean = false,
        traceIndex: Int
    ) {
        var resolvedTag = tag ?: "Unknown"
        val metadata = mutableMapOf<String, Any>()

        val trace = Throwable().stackTrace.getOrNull(traceIndex)
        if (trace != null) {
            val file = trace.fileName
            if (file != null) metadata["file"] = file
            metadata["line"] = trace.lineNumber
            metadata["class"] = trace.className
            metadata["method"] = trace.methodName

            if (tag == null) {
                val strippedClass = trace.className.substringAfterLast(".").substringBefore("$")
                resolvedTag = if (!strippedClass.endsWith("Kt")) {
                    strippedClass
                } else {
                    strippedClass.substringAfterLast(".").substringBeforeLast("Kt")
                }
            }
        }

        log(message, resolvedTag, level, secure, metadata)
    }

    fun log(message: Any?, tag: String? = defaultTag, level: LogLevel, secure: Boolean = false) = logWithoutMetadata(message, tag, level, secure, 4)
    fun fault(message: Any?, tag: String? = defaultTag, secure: Boolean = false) = log(message, tag, LogLevel.FAULT, secure)
    fun error(message: Any?, tag: String? = defaultTag, secure: Boolean = false) = log(message, tag, LogLevel.ERROR, secure)
    fun info(message: Any?, tag: String? = defaultTag, secure: Boolean = false) = log(message, tag, LogLevel.INFO, secure)
    fun debug(message: Any?, tag: String? = defaultTag, secure: Boolean = false) = log(message, tag, LogLevel.DEBUG, secure)

    companion object {
        private val shared = Logger()

        fun flow(tag: String? = shared.defaultTag, level: LogLevel = LogLevel.DEBUG) = shared.flow(tag, level)
        fun tagged(tag: String) = shared.tagged(tag)

        fun log(message: Any?, tag: String, level: LogLevel, secure: Boolean = false, metadata: Map<String, Any> = mapOf()) =
            shared.log(message, tag, level, secure, metadata)
        fun log(message: Any?, tag: String? = shared.defaultTag, level: LogLevel, secure: Boolean = false) =
            shared.logWithoutMetadata(message, tag, level, secure, 5)
        fun fault(message: Any?, tag: String? = shared.defaultTag, secure: Boolean = false) = log(message, tag, LogLevel.FAULT, secure)
        fun error(message: Any?, tag: String? = shared.defaultTag, secure: Boolean = false) = log(message, tag, LogLevel.ERROR, secure)
        fun info(message: Any?, tag: String? = shared.defaultTag, secure: Boolean = false) = log(message, tag, LogLevel.INFO, secure)
        fun debug(message: Any?, tag: String? = shared.defaultTag, secure: Boolean = false) = log(message, tag, LogLevel.DEBUG, secure)
    }
}