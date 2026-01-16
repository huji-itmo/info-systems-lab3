package com.example.fileService

import org.hibernate.boot.MetadataBuilder
import org.hibernate.boot.registry.StandardServiceRegistry
import org.hibernate.boot.spi.MetadataBuilderInitializer
import org.hibernate.cache.spi.access.AccessType

class EntityCachingMetadataBuilderInitializer : MetadataBuilderInitializer {
    override fun contribute(
        metadataBuilder: MetadataBuilder,
        serviceRegistry: StandardServiceRegistry,
    ) {
    }
}
