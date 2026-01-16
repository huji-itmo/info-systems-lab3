package com.example.fileService.service

import com.example.fileService.repositories.ChapterRepository
import org.example.shared.model.Chapter
import org.example.shared.model.dto.ChapterEmbedded
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChapterTransactionService(
    private val chapterRepository: ChapterRepository,
) {
    @Transactional
    fun existsById(id: Long): Boolean = chapterRepository.existsById(id)

    @Transactional
    fun prepareCreate(chapterDto: ChapterEmbedded): Long {
        // Validate chapter data
        if (chapterDto.name.isBlank()) {
            throw IllegalArgumentException("Chapter name cannot be blank")
        }

        if (chapterDto.marinesCount < 0) {
            throw IllegalArgumentException("Marines count cannot be negative")
        }

        // Check if chapter with same name already exists
        if (chapterRepository.existsByNameIgnoreCase(chapterDto.name)) {
            throw IllegalArgumentException("Chapter with name '${chapterDto.name}' already exists")
        }

        // Create temporary chapter record to reserve ID
        val chapter =
            Chapter(
                name = chapterDto.name,
                marinesCount = chapterDto.marinesCount,
            )
        return chapterRepository.save(chapter).id
    }

    @Transactional
    fun commitCreate(chapterDto: ChapterEmbedded) {
        // In this implementation, chapter is already created in prepare phase
        // This method is called to confirm the operation
        // Could be extended to move from staging to production table
    }

    @Transactional
    fun deleteById(id: Long) {
        chapterRepository.deleteById(id)
    }
}
