package com.epam.test.multilingualgenerationsystem

import com.epam.test.multilingualgenerationsystem.plugins.configureCors
import com.epam.test.multilingualgenerationsystem.plugins.configureDatabase
import com.epam.test.multilingualgenerationsystem.plugins.configureRouting
import com.epam.test.multilingualgenerationsystem.plugins.configureSerialization
import com.epam.test.multilingualgenerationsystem.repository.TranslationMemoryRepository
import com.epam.test.multilingualgenerationsystem.services.BatchTranslationService
import com.epam.test.multilingualgenerationsystem.services.FileStorageService
import com.epam.test.multilingualgenerationsystem.services.TranslationService
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

const val SERVER_PORT = 8080

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureCors()
    configureSerialization()
    configureDatabase()
    
    val tmRepository = TranslationMemoryRepository()
    val translationService = TranslationService(tmRepository)
    val batchTranslationService = BatchTranslationService(translationService)
    val fileStorageService = FileStorageService()
    
    configureRouting(batchTranslationService, fileStorageService)
}