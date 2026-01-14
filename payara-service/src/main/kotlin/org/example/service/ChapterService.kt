package org.example.service

import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.transaction.Transactional
import jakarta.validation.Valid
import org.example.exceptions.NotFoundException
import org.example.interceptor.MonitoredQuery
import org.example.shared.model.Chapter
import org.example.shared.model.dto.Page
import kotlin.math.ceil

@ApplicationScoped
open class ChapterService {
    @PersistenceContext(unitName = "my-pu")
    private lateinit var em: EntityManager

    @MonitoredQuery
    open fun findAll(
        page: Int,
        size: Int,
    ): Page<Chapter> {
        val total = em.createQuery("SELECT COUNT(c) FROM Chapter c", Long::class.java)
            .setHint("eclipselink.query-results-cache", true)
            .setHint("eclipselink.query-results-cache.expiry", "3600000") // 1 hour
            .singleResult

        val content =
            em
                .createQuery("SELECT c FROM Chapter c", Chapter::class.java)
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
    open fun findById(id: Long): Chapter =
        em.find(
            Chapter::class.java,
            id,
            mapOf(
                "eclipselink.cache-usage" to "CheckCacheByPrimaryKey",
                "eclipselink.refresh" to "false", // Don't refresh from DB if in cache
                "eclipselink.read-only" to "true" // Optimize for read-only access
            )
        ) ?: throw NotFoundException("Chapter not found with id: $id")

    @Transactional
    open fun create(
        @Valid chapter: Chapter,
    ): Chapter {
        val entity =
            Chapter(
                id = 0, // auto-generated
                name = chapter.name,
                marinesCount = chapter.marinesCount,
            )
        em.persist(entity)
        return entity
    }

    @Transactional
    open fun update(
        id: Long,
        @Valid chapter: Chapter,
    ): Chapter {
        findById(id) // ensure exists
        val updated =
            Chapter(
                id = id,
                name = chapter.name,
                marinesCount = chapter.marinesCount,
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