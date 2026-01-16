package com.example.fileService

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EntityScan(basePackages = ["org.example.shared.model", "com.example.fileService.model"])
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableScheduling
object FileServiceApplication {
    @JvmStatic
    fun main(args: Array<String>) {
        SpringApplication.run(FileServiceApplication::class.java, *args)
    }
}
