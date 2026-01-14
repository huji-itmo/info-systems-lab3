package org.example.service

import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.transaction.Transactional
import jakarta.validation.Valid
import org.example.exceptions.NotFoundException
import org.example.interceptor.MonitoredQuery
import org.example.shared.model.Chapter
import org.example.shared.model.Coordinates
import org.example.shared.model.dto.Page
import kotlin.math.ceil

@ApplicationScoped
open class CoordinatesService {
    @PersistenceContext(unitName = "my-pu")
    private lateinit var em: EntityManager

    @MonitoredQuery
    open fun findAll(
        page: Int,
        size: Int,
    ): Page<Coordinates> {
        val total = em.createQuery("SELECT COUNT(c) FROM Coordinates c", Long::class.java)
            .setHint("eclipselink.query-results-cache", true)
            .setHint("eclipselink.query-results-cache.expiry", "3600000") // 1 hour
            .singleResult

        val content =
            em
                .createQuery("SELECT c FROM Coordinates c", Coordinates::class.java)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .setHint("eclipselink.query-results-cache", true)
                .setHint("eclipselink.query-results-cache.expiry", "3600000") // 1 hour
                .setHint("eclipselink.query-results-cache.including-parameter-values", true)
                .resultList

        return Page(
            content = content,
            page = page,
            size = size,
            totalElements = total,
            totalPages = ceil(total.toDouble() / size).toInt(),
        )
    }

    @MonitoredQuery
    open fun findById(id: Long): Coordinates =
        em.find(
            Coordinates::class.java,
            id,
            mapOf(
                "eclipselink.cache-usage" to "CheckCacheByPrimaryKey",
                "eclipselink.refresh" to "false",
                "eclipselink.read-only" to "true"
            )
        ) ?: throw NotFoundException("Coordinates not found with id: $id")

    @Transactional
    open fun create(
        @Valid coordinates: Coordinates,
    ): Coordinates {
        val entity =
            Coordinates(
                id = 0,
                x = coordinates.x,
                y = coordinates.y,
            )
        em.persist(entity)
        return entity
    }

    @Transactional
    open fun update(
        id: Long,
        @Valid coordinates: Coordinates,
    ): Coordinates {
        findById(id)
        val updated =
            Coordinates(
                id = id,
                x = coordinates.x,
                y = coordinates.y,
            )
        em.merge(updated)
        return updated
    }

    @Transactional
    open fun delete(id: Long) {
        val entity = findById(id)
        em.remove(entity)
    }
}
