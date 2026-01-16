package com.example.fileService.service

import com.example.fileService.repositories.CoordinatesRepository
import org.example.shared.model.Coordinates
import org.example.shared.model.dto.CoordinatesEmbedded
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
open class CoordinatesTransactionService(
    private val coordinatesRepository: CoordinatesRepository,
) {
    @Transactional
    open fun existsById(id: Long): Boolean = coordinatesRepository.existsById(id)

    @Transactional
    open fun prepareCreate(coordinatesDto: CoordinatesEmbedded): Long {
        // Validate coordinates data
        // Y can be null based on the Coordinates model

        // Check if identical coordinates already exist
//        if (coordinatesRepository.existsByXAndY(coordinatesDto.x.toFloat(), coordinatesDto.y?.toFloat())) {
//            throw IllegalArgumentException("Coordinates (${coordinatesDto.x}, ${coordinatesDto.y}) already exist")
//        }

        // Create temporary coordinates record to reserve ID
        val coordinates =
            Coordinates(
                x = coordinatesDto.x,
                y = coordinatesDto.y,
            )
        return coordinatesRepository.save(coordinates).id
    }

    @Transactional
    open fun commitCreate(coordinatesDto: CoordinatesEmbedded) {
        // In this implementation, coordinates are already created in prepare phase
        // This method is called to confirm the operation
        // Could be extended to move from staging to production table
    }

    @Transactional
    open fun deleteById(id: Long) {
        coordinatesRepository.deleteById(id)
    }
}
