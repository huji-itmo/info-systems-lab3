package com.example.fileService.service

import com.example.fileService.repositories.SpaceMarineRepository
import org.example.shared.model.SpaceMarine
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
open class CacheLoggingTestService(
    private val spaceMarineRepository: SpaceMarineRepository,
) {
    private val logger = LoggerFactory.getLogger(CacheLoggingTestService::class.java)

    fun testCacheLogging() {
        logger.info("=== Testing Cache Logging Functionality ===")

        // Create a test SpaceMarine
        val testMarine =
            SpaceMarine(
                name = "Test Marine",
                coordinatesId = 1L,
                chapterId = 1L,
                health = 100L,
                weaponType = org.example.shared.model.Weapon.BOLTGUN,
            )

        // Save the marine
        val savedMarine = spaceMarineRepository.save(testMarine)
        logger.info("Saved marine with ID: ${savedMarine.id}")

        // First access - should be a cache miss
        logger.info("First access (expected cache miss):")
        val firstAccess = spaceMarineRepository.findById(savedMarine.id!!.toLong())

        // Second access - should be a cache hit
        logger.info("Second access (expected cache hit):")
        val secondAccess = spaceMarineRepository.findById(savedMarine.id!!.toLong())

        // Third access - should be a cache hit
        logger.info("Third access (expected cache hit):")
        val thirdAccess = spaceMarineRepository.findById(savedMarine.id!!.toLong())

        logger.info("=== Cache Logging Test Complete ===")
    }
}
