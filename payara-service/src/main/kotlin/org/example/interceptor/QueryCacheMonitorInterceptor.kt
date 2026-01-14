package org.example.interceptor

import jakarta.interceptor.AroundInvoke
import jakarta.interceptor.Interceptor
import jakarta.interceptor.InvocationContext
import java.time.Duration
import java.time.Instant
import java.util.logging.Logger
import jakarta.enterprise.context.Dependent
import jakarta.persistence.Query
import org.eclipse.persistence.config.QueryHints
import org.eclipse.persistence.sessions.Session
import org.eclipse.persistence.internal.sessions.AbstractSession

@Interceptor
@MonitoredQuery
@Dependent
class QueryCacheMonitorInterceptor {

    private val logger = Logger.getLogger(QueryCacheMonitorInterceptor::class.java.name)

    @AroundInvoke
    fun monitorQuery(context: InvocationContext): Any {
        val start = Instant.now()
        val className = context.target?.javaClass?.simpleName ?: "UnknownClass"
        val methodName = context.method.name

        logger.fine("Executing query method: $className.$methodName")

        try {
            val result = context.proceed()
            val duration = Duration.between(start, Instant.now())

            // Log execution time regardless of cache status
            logger.info("[$className.$methodName] executed in ${duration.toMillis()}ms")

            return result
        } catch (e: Exception) {
            val duration = Duration.between(start, Instant.now())
            logger.severe("Exception in $className.$methodName after ${duration.toMillis()}ms: ${e.message}")
            throw e
        }
    }
}