package org.example.service

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.transaction.Transactional
import jakarta.validation.Valid
import org.example.exceptions.NotFoundException
import org.example.interceptor.MonitoredQuery
import org.example.shared.model.AstartesCategory
import org.example.shared.model.Chapter
import org.example.shared.model.Coordinates
import org.example.shared.model.dto.Page
import org.example.shared.model.SpaceMarine
import org.example.shared.model.Weapon
import org.example.shared.model.dto.SpaceMarineCreateRequest
import org.example.shared.model.dto.SpaceMarineUpdateRequest
import java.util.logging.Logger
import kotlin.math.ceil

@ApplicationScoped
open class SpaceMarineService {
    @PersistenceContext(unitName = "my-pu")
    private lateinit var em: EntityManager

    @Inject
    private lateinit var chapterService: ChapterService

    @Inject
    private lateinit var coordinatesService: CoordinatesService

    companion object {
        private val logger = Logger.getLogger(SpaceMarineService::class.java.name)
    }

    @MonitoredQuery
    open fun findAll(
        page: Int,
        size: Int,
    ): Page<SpaceMarine> {
        val total = em.createQuery("SELECT COUNT(s) FROM SpaceMarine s", Long::class.java)
            .setHint("eclipselink.query-results-cache", "true")
            .setHint("eclipselink.query-results-cache.expiry", "3600000") // 1 hour
            .singleResult

        val content =
            em
                .createQuery("SELECT s FROM SpaceMarine s", SpaceMarine::class.java)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .setHint("eclipselink.query-results-cache", "true")
                .setHint("eclipselink.query-results-cache.expiry", "3600000") // 1 hour
                .setHint("eclipselink.query-results-cache.including-parameter-values", "true")
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
    open fun findById(id: Int): SpaceMarine =
        em.find(
            SpaceMarine::class.java,
            id,
            mapOf(
                "eclipselink.cache-usage" to "CheckCacheByPrimaryKey",
                "eclipselink.refresh" to "false",
                "eclipselink.read-only" to "true"
            )
        ) ?: throw NotFoundException("SpaceMarine not found with id: $id")

    @MonitoredQuery
    open fun findByWeaponTypes(
        weaponTypes: List<Weapon>,
        page: Int,
        size: Int,
    ): Page<SpaceMarine> {
        logger.info("Filtering marines by weapons: $weaponTypes (page=$page, size=$size)")

        if (weaponTypes.isEmpty()) {
            return Page(emptyList(), page, size, 0, 0)
        }

        // Query for total count with caching
        val countQuery =
            em
                .createQuery(
                    "SELECT COUNT(s) FROM SpaceMarine s WHERE s.weaponType IN :weapons",
                    Long::class.java,
                )
                .setParameter("weapons", weaponTypes)
                .setHint("eclipselink.query-results-cache", true)
                .setHint("eclipselink.query-results-cache.expiry", "1800000") // 30 minutes
                .setHint("eclipselink.query-results-cache.including-parameter-values", true)

        val total = countQuery.singleResult

        // Query for content with caching and ordering
        val contentQuery =
            em.createQuery(
                "SELECT s FROM SpaceMarine s WHERE s.weaponType IN :weapons ORDER BY s.name",
                SpaceMarine::class.java,
            )
                .setParameter("weapons", weaponTypes)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .setHint("eclipselink.query-results-cache", true)
                .setHint("eclipselink.query-results-cache.expiry", "1800000") // 30 minutes
                .setHint("eclipselink.query-results-cache.including-parameter-values", true)

        val content = contentQuery.resultList

        return Page(
            content = content,
            page = page,
            size = size,
            totalElements = total,
            totalPages = ceil(total.toDouble() / size).toInt(),
        )
    }

    @MonitoredQuery
    open fun sumHealth(): Long {
        logger.info("Calculating sum of health values")
        return em
            .createQuery("SELECT COALESCE(SUM(s.health), 0) FROM SpaceMarine s", Long::class.java)
            .setHint("eclipselink.query-results-cache", true)
            .setHint("eclipselink.query-results-cache.expiry", "600000") // 10 minutes
            .singleResult
    }

    @MonitoredQuery
    open fun averageHealth(): Double {
        logger.info("Calculating average health value")
        val avg = em.createQuery("SELECT AVG(s.health) FROM SpaceMarine s", Double::class.java)
            .setHint("eclipselink.query-results-cache", true)
            .setHint("eclipselink.query-results-cache.expiry", "600000") // 10 minutes
            .singleResult
        return avg ?: 0.0
    }

    @Transactional
    open fun update(
        id: Int,
        @Valid update: SpaceMarineUpdateRequest,
    ): SpaceMarine {
        val existing = findById(id)

        // Convert string values to enums with null safety
        val category =
            update.category?.let { categoryStr ->
                try {
                    AstartesCategory.valueOf(categoryStr.uppercase())
                } catch (e: IllegalArgumentException) {
                    throw IllegalArgumentException(
                        "Invalid category value: $categoryStr. " +
                            "Valid values are: ${AstartesCategory.values().joinToString()}",
                    )
                }
            } ?: existing.category

        val weaponType =
            update.weaponType?.let { weaponStr ->
                try {
                    Weapon.valueOf(weaponStr.uppercase())
                } catch (e: IllegalArgumentException) {
                    throw IllegalArgumentException(
                        "Invalid weapon type: $weaponStr. " +
                            "Valid values are: ${Weapon.values().joinToString()}",
                    )
                }
            } ?: existing.weaponType

        val updated =
            SpaceMarine(
                id = id,
                name = update.name ?: existing.name,
                coordinatesId = update.coordinatesId ?: existing.coordinatesId,
                chapterId = update.chapterId ?: existing.chapterId,
                health = update.health ?: existing.health,
                loyal = update.loyal ?: existing.loyal,
                category = category,
                weaponType = weaponType,
                creationDate = existing.creationDate, // immutable
            )
        em.merge(updated)
        return updated
    }

    @Transactional
    open fun delete(id: Int) {
        val entity = findById(id)
        em.remove(entity)
    }

    @Transactional
    open fun assignMarineToChapter(
        marineId: Int,
        chapterId: Long,
    ): SpaceMarine {
        logger.info("Assigning marine $marineId to chapter $chapterId")
        val marine = findById(marineId)

        // Валидация существования ордена
        try {
            chapterService.findById(chapterId)
        } catch (e: NotFoundException) {
            throw NotFoundException("Chapter with ID $chapterId not found")
        }

        marine.chapterId = chapterId
        return em.merge(marine)
    }

    @Transactional
    open fun create(
        @Valid spaceMarine: SpaceMarineCreateRequest,
    ): SpaceMarine {
        // Convert string values to enums safely
        val category =
            spaceMarine.category?.let {
                try {
                    if (it == "null") {
                        return@let null
                    }
                    AstartesCategory.valueOf(it.uppercase())
                } catch (e: IllegalArgumentException) {
                    throw IllegalArgumentException(
                        "Invalid category value: $it. " +
                                "Valid values are: ${AstartesCategory.entries.joinToString()}",
                    )
                }
            }

        val weaponType =
            try {
                Weapon.valueOf(spaceMarine.weaponType.uppercase())
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException(
                    "Invalid weapon type: ${spaceMarine.weaponType}. " +
                            "Valid values are: ${Weapon.entries.joinToString()}",
                )
            }

        val entity =
            SpaceMarine(
                id = 0,
                name = spaceMarine.name,
                coordinatesId = spaceMarine.coordinatesId,
                chapterId = spaceMarine.chapterId,
                health = spaceMarine.health,
                loyal = spaceMarine.loyal,
                category = category,
                weaponType = weaponType,
            )
        em.persist(entity)
        return entity
    }

}
