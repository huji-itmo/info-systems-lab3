package org.example.shared.model

import jakarta.persistence.Cacheable
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.Max

@Entity
@Table(name = "coordinates")
@Cacheable(true)
data class Coordinates(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    var x: Long,
    @field:Max(343)
    var y: Float,
)
