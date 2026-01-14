package com.example.fileService.repositories

import com.example.fileService.model.ImportHistory
import jakarta.persistence.QueryHint
import org.example.shared.model.Chapter
import org.example.shared.model.Coordinates
import org.springframework.data.jpa.repository.QueryHints
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface ChapterRepository : CrudRepository<Chapter, Long> {

    @QueryHints(
        value = [
            QueryHint(
                name = "org.hibernate.cacheable",
                value = "true"
            )
        ]
    )
    override fun findById(id: Long): Optional<Chapter>
}
