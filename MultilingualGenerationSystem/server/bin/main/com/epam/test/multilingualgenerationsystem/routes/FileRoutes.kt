package com.epam.test.multilingualgenerationsystem.routes

import com.epam.test.multilingualgenerationsystem.models.FileFormat
import com.epam.test.multilingualgenerationsystem.models.GenerateRequest
import com.epam.test.multilingualgenerationsystem.models.TranslationKey
import com.epam.test.multilingualgenerationsystem.parser.FileParserFactory
import com.epam.test.multilingualgenerationsystem.services.BatchTranslationService
import com.epam.test.multilingualgenerationsystem.services.FileStorageService
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.fileRoutes(
    batchTranslationService: BatchTranslationService,
    fileStorageService: FileStorageService
) {
    route("/api/v1") {
        post("/upload") {
            val multipart = call.receiveMultipart()
            var fileName = ""
            var fileBytes: ByteArray? = null
            var sourceLanguage = "en" // Default

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FileItem -> {
                        fileName = part.originalFileName ?: "unknown"
                        fileBytes = part.streamProvider().readBytes()
                    }
                    is PartData.FormItem -> {
                        if (part.name == "sourceLanguage") {
                            sourceLanguage = part.value
                        }
                    }
                    else -> {}
                }
                part.dispose()
            }

            if (fileBytes != null && fileName.isNotEmpty()) {
                val fileId = fileStorageService.saveFile(fileName, fileBytes!!)
                call.respond(HttpStatusCode.OK, mapOf(
                    "fileId" to fileId,
                    "fileName" to fileName,
                    "sourceLanguage" to sourceLanguage
                ))
            } else {
                call.respond(HttpStatusCode.BadRequest, "No file uploaded")
            }
        }

        post("/generate") {
            try {
                // Log raw body for debugging
                val rawBody = call.receiveText()
                println("Received /generate request body: $rawBody")
                
                // We need to re-receive or manually parse since receiveText consumes the stream.
                // However, receive<T> might check the stream. 
                // Better to parse the rawBody manually if we consumed it, 
                // OR just use ContentNegotiation and catch the error.
                // Let's rely on Ktor's double receive if configured, or just parse the string.
                // To avoid DoubleReceive configuration issues, let's just use the json serializer directly.
                
                val request = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }.decodeFromString<GenerateRequest>(rawBody)
                
                val sourceFile = fileStorageService.getFile(request.sourceFileId)
                if (sourceFile == null || !sourceFile.exists()) {
                    call.respond(HttpStatusCode.NotFound, "Source file not found")
                    return@post
                }

                try {
                    val parser = FileParserFactory.getParser(sourceFile.name)
                    val parseResult = parser.parse(sourceFile.readText())
                    
                    val generationId = batchTranslationService.processBatch(
                        parseResult.keys,
                        "en", // Assuming 'en' for now, ideally passed in request or stored with file
                        request.targetLanguages,
                        request.outputFormats
                    )
                    call.respond(mapOf("generationId" to generationId))
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError, "Error processing file: ${e.message}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.BadRequest, "Invalid request body: ${e.message}")
            }
        }

        get("/download/{generationId}") {
            val generationId = call.parameters["generationId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val file = batchTranslationService.getZipFile(generationId)
            if (file.exists()) {
                call.response.header(
                    HttpHeaders.ContentDisposition,
                    ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, "${generationId}.zip").toString()
                )
                call.respondFile(file)
            } else {
                call.respond(HttpStatusCode.NotFound, "Generation not found")
            }
        }
    }
}

