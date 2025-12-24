package com.epam.test.multilingualgenerationsystem.plugins

import com.epam.test.multilingualgenerationsystem.routes.fileRoutes
import com.epam.test.multilingualgenerationsystem.services.BatchTranslationService
import com.epam.test.multilingualgenerationsystem.services.FileStorageService
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.HttpStatusCode
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.TransactionManager

fun Application.configureRouting(
    batchTranslationService: BatchTranslationService,
    fileStorageService: FileStorageService
) {
    routing {
        get("/") {
            call.respondText("Ktor is running!")
        }
        fileRoutes(batchTranslationService, fileStorageService)
        get("/health") {
            try {
                newSuspendedTransaction {
                    TransactionManager.current().exec("SELECT 1")
                }
                call.respond(HttpStatusCode.OK, "Database connection is healthy")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Database connection failed: ${e.message}")
            }
        }
    }
}
