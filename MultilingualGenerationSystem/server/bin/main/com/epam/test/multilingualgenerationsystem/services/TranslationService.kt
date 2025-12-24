package com.epam.test.multilingualgenerationsystem.services

import com.epam.test.multilingualgenerationsystem.models.TranslationKey
import com.epam.test.multilingualgenerationsystem.repository.TranslationMemoryRepository
import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*

@Serializable
private data class ChatCompletionRequest(
    val messages: List<ChatMessage>,
    val model: String? = null,
    val temperature: Double = 0.3
)

@Serializable
private data class ChatMessage(
    val role: String,
    val content: String
)

@Serializable
private data class ChatCompletionResponse(
    val choices: List<Choice>
)

@Serializable
private data class Choice(
    val message: ChatMessage
)

class TranslationService(private val repository: TranslationMemoryRepository) {
    private val client = HttpClient(Java) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun translateBatch(
        keys: List<TranslationKey>,
        sourceLanguage: String,
        targetLanguage: String
    ): List<TranslationKey> = coroutineScope {
        keys.chunked(5).flatMap { chunk ->
            chunk.map { key ->
                async {
                    translateSingle(key, sourceLanguage, targetLanguage)
                }
            }.awaitAll()
        }
    }

    private suspend fun translateSingle(
        key: TranslationKey,
        sourceLanguage: String,
        targetLanguage: String
    ): TranslationKey {
        // Check Translation Memory first
        val cachedTranslation = repository.findTranslation(key.value, sourceLanguage, targetLanguage)
        if (cachedTranslation != null) {
            println("Found cached translation for '${key.key}': $cachedTranslation")
            return key.copy(value = cachedTranslation)
        }

        // Using a simplified prompt format suitable for instruction-tuned models
        val prompt = "Translate the following text from $sourceLanguage to $targetLanguage. Only output the translated text, with no additional information or explanation.\n\nText: \"${key.value}\""

        val request = ChatCompletionRequest(
            messages = listOf(
                ChatMessage(role = "user", content = prompt)
            )
        )

        try {
            println("Sending translation request for key: ${key.key}")
            // println("Request body: ${json.encodeToString(ChatCompletionRequest.serializer(), request)}")

            val response: HttpResponse = client.post("http://localhost:5002/v1/chat/completions") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            // println("Received response for key: ${key.key}, status: ${response.status}")
            val responseBody = response.bodyAsText()
            // println("Response body: $responseBody")

            if (response.status == HttpStatusCode.OK) {
                val completionResponse = json.decodeFromString<ChatCompletionResponse>(responseBody)
                val translatedText = completionResponse.choices.firstOrNull()?.message?.content?.trim() ?: key.value
                
                // Save to Translation Memory
                repository.saveTranslation(key.value, translatedText, sourceLanguage, targetLanguage)
                
                return key.copy(value = translatedText)
            } else {
                println("Translation failed for key ${key.key} with status ${response.status}: $responseBody")
                return key
            }
        } catch (e: Exception) {
            println("Translation failed for key ${key.key}: ${e.message}")
            return key
        }
    }
}