package com.example.fileService.repositories

import com.example.fileService.annotation.CacheLogged
import com.example.fileService.model.ImportHistory
import jakarta.persistence.QueryHint
import org.example.shared.model.SpaceMarine
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface SpaceMarineRepository : CrudRepository<SpaceMarine, Long> {
    @CacheLogged(entityName = "SpaceMarine", logArgs = true, thresholdMs = 50)
    @QueryHints(
        value = [
            QueryHint(
                name = "org.hibernate.cacheable",
                value = "true",
            ),
        ],
    )
    override fun findById(id: Long): Optional<SpaceMarine>

    fun existsByNameIgnoreCase(name: String): Boolean
}
