package com.example.fileService

import org.hibernate.boot.MetadataBuilder
import org.hibernate.boot.registry.StandardServiceRegistry
import org.hibernate.boot.spi.MetadataBuilderInitializer
import org.hibernate.cache.spi.access.AccessType

class EntityCachingMetadataBuilderInitializer : MetadataBuilderInitializer {

    override fun contribute(p0: MetadataBuilder, p1: StandardServiceRegistry) {
//        p0.apply {
//            applyCache("org.example.shared.model.Chapter", AccessType.READ_WRITE)
//            applyCache("org.example.shared.model.Coordinates", AccessType.READ_WRITE)
//            applyCache("org.example.shared.model.SpaceMarine", AccessType.READ_WRITE)
//        }
    }
}