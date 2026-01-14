package com.example.fileService.repositories

import com.example.fileService.model.ImportHistory
import jakarta.persistence.QueryHint
import org.example.shared.model.SpaceMarine
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface SpaceMarineRepository : CrudRepository<SpaceMarine, Long> {
    @QueryHints(
        value = [
            QueryHint(
                name = "org.hibernate.cacheable",
                value = "true"
            )
        ]
    )
    override fun findById(id: Long): Optional<SpaceMarine>
}