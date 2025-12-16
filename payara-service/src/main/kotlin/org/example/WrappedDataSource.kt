package org.example

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import jakarta.annotation.sql.DataSourceDefinition
import org.postgresql.ds.PGSimpleDataSource
import java.io.PrintWriter
import java.sql.Connection
import java.sql.SQLException
import java.sql.SQLFeatureNotSupportedException
import java.util.logging.Logger
import javax.sql.DataSource

@DataSourceDefinition(
    name = "java:app/jdbc/studsDS",  // Match the JNDI name from glassfish-resources.xml
    className = "org.example.WrappedDataSource"
)
open class WrappedDataSource : DataSource {

    private val hikariDataSource: HikariDataSource

    init {
        // Get environment variables (matching GlassFish config)
        val postgresHost = System.getenv("POSTGRES_HOST") ?: "localhost"
        val postgresUser = System.getenv("POSTGRES_USER") ?: "postgres"
        val postgresPassword = System.getenv("POSTGRES_PASSWORD") ?: ""

        // Configure PostgreSQL DataSource
        val pgDataSource = PGSimpleDataSource().apply {
            serverNames = arrayOf(postgresHost)
            portNumbers = intArrayOf(5432)
            databaseName = "studs"
            user = postgresUser
            password = postgresPassword
        }

        val config = HikariConfig().apply {
            dataSource = pgDataSource

            poolName = "studs-pool"  // Match the pool name from glassfish-resources.xml
            minimumIdle = 5
            maximumPoolSize = 20
            connectionTimeout = 30_000 // 30 seconds
            idleTimeout = 600_000 // 10 minutes
            maxLifetime = 1800_000 // 30 minutes
            connectionTestQuery = "SELECT 1"
            isRegisterMbeans = true

            validationTimeout = 3000
            leakDetectionThreshold = 60_000 // 1 minute
        }

        hikariDataSource = HikariDataSource(config)

        // Optional: Add shutdown hook to close the pool properly
        Runtime.getRuntime().addShutdownHook(Thread {
            if (!hikariDataSource.isClosed) {
                hikariDataSource.close()
            }
        })
    }

    @Throws(SQLException::class)
    override fun getConnection(): Connection = hikariDataSource.connection

    @Throws(SQLException::class)
    override fun getConnection(username: String, password: String): Connection =
        hikariDataSource.getConnection(username, password)

    @Throws(SQLException::class)
    override fun getLogWriter(): PrintWriter? = hikariDataSource.logWriter

    @Throws(SQLException::class)
    override fun setLogWriter(out: PrintWriter?) {
        hikariDataSource.logWriter = out
    }

    @Throws(SQLException::class)
    override fun setLoginTimeout(seconds: Int) {
        hikariDataSource.loginTimeout = seconds
    }

    @Throws(SQLException::class)
    override fun getLoginTimeout(): Int = hikariDataSource.loginTimeout

    @Throws(SQLFeatureNotSupportedException::class)
    override fun getParentLogger(): Logger = hikariDataSource.parentLogger

    @Throws(SQLException::class)
    override fun <T> unwrap(iface: Class<T>): T = hikariDataSource.unwrap(iface)

    @Throws(SQLException::class)
    override fun isWrapperFor(iface: Class<*>): Boolean = hikariDataSource.isWrapperFor(iface)
}