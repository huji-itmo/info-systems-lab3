package com.example.fileService.repositories

import com.example.fileService.model.ImportHistory
import jakarta.persistence.QueryHint
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface ImportHistoryRepository : JpaRepository<ImportHistory, Long> {
    @QueryHints(
        value = [
            QueryHint(
                name = "org.hibernate.cacheable",
                value = "true"
            )
        ]
    )
    override fun findById(id: Long): Optional<ImportHistory>
}