package com.example.fileService.service

import com.example.fileService.model.SpaceMarineImportRequest
import com.example.fileService.repositories.SpaceMarineRepository
import org.example.shared.model.SpaceMarine
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
open class SpaceMarineTransactionService(
    private val spaceMarineRepository: SpaceMarineRepository,
) {
    @Transactional
    open fun create(
        request: SpaceMarineImportRequest,
        chapterId: Long,
        coordinatesId: Long,
    ): Long {
        // Validate that SpaceMarine with same name doesn't exist
//        if (spaceMarineRepository.existsByNameIgnoreCase(request.name)) {
//            throw IllegalArgumentException("Space Marine with name '${request.name}' already exists")
//        }

        val spaceMarine =
            SpaceMarine(
                name = request.name,
                health = request.health,
                category = request.category,
                chapterId = chapterId,
                coordinatesId = coordinatesId,
                weaponType = request.weaponType,
                loyal = request.loyal,
            )

        val savedMarine = spaceMarineRepository.save(spaceMarine)
        return savedMarine.id.toLong()
    }

    @Transactional
    open fun deleteById(id: Long) {
        spaceMarineRepository.deleteById(id)
    }
}
