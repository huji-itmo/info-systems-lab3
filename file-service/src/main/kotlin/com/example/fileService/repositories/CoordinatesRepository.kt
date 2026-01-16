package com.example.fileService.repositories

import com.example.fileService.annotation.CacheLogged
import jakarta.persistence.QueryHint
import org.example.shared.model.Coordinates
import org.example.shared.model.SpaceMarine
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface CoordinatesRepository : CrudRepository<Coordinates, Long> {
    @CacheLogged(entityName = "Coordinates", thresholdMs = 25)
    @QueryHints(
        value = [
            QueryHint(
                name = "org.hibernate.cacheable",
                value = "true",
            ),
        ],
    )
    override fun findById(id: Long): Optional<Coordinates>

    fun existsByXAndY(
        x: Float,
        y: Float?,
    ): Boolean
}
