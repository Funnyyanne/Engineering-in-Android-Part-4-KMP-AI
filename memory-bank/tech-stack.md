# Multilingual Generation System - Technology Stack Recommendations

## Executive Summary

This document provides detailed technology stack recommendations for building the Multilingual Generation System **locally on macOS**, with a focus on **Kotlin Multiplatform (KMP)** for maximum code sharing between backend, Android, iOS, and desktop applications.

---

## 🎯 Local Development Stack Overview

| Layer | Technology | Why KMP-Friendly |
|-------|------------|------------------|
| **Backend** | Ktor (KMP) | Native Kotlin, shares code with clients |
| **Frontend Web** | Compose for Web (KMP) | Same UI code as Android/Desktop |
| **Database** | PostgreSQL + SQLDelight | SQLDelight is KMP-native |
| **File Storage** | Local filesystem | Simple for development |
| **Translation AI** | OpenAI API | Kotlin client available |

---

## Recommended Architecture (Local KMP-Focused)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         KOTLIN MULTIPLATFORM SHARED                          │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │  commonMain: Models, DTOs, Validation, Translation Logic            │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
          │                    │                    │                    │
          ▼                    ▼                    ▼                    ▼
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────┐
│   Ktor Server   │  │  Compose Web    │  │ Android/Desktop │  │  iOS App    │
│   (Backend)     │  │  (Dashboard)    │  │   (Optional)    │  │ (Optional)  │
│   jvmMain       │  │   wasmJsMain    │  │  androidMain    │  │  iosMain    │
└────────┬────────┘  └─────────────────┘  └─────────────────┘  └─────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         LOCAL INFRASTRUCTURE                                 │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐             │
│  │   PostgreSQL    │  │     Redis       │  │  Local Files    │             │
│  │   (Docker)      │  │   (Docker)      │  │  ~/multilingual │             │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 1. KMP Project Structure

### 1.1 Recommended Module Layout

```
multilingual-system/
├── shared/                          # KMP Shared Module
│   ├── build.gradle.kts
│   └── src/
│       ├── commonMain/              # Shared across ALL platforms
│       │   └── kotlin/
│       │       ├── models/          # Data classes
│       │       ├── dto/             # API DTOs
│       │       ├── repository/      # Repository interfaces
│       │       ├── parser/          # File parsers (XML, JSON, etc.)
│       │       ├── generator/       # File generators
│       │       └── util/            # Utilities
│       ├── jvmMain/                 # JVM-specific (Backend)
│       ├── wasmJsMain/              # Web (Compose for Web)
│       ├── androidMain/             # Android-specific
│       ├── iosMain/                 # iOS-specific
│       └── commonTest/              # Shared tests
│
├── backend/                         # Ktor Server (jvmMain)
│   ├── build.gradle.kts
│   └── src/
│       └── main/
│           └── kotlin/
│               ├── Application.kt
│               ├── plugins/         # Ktor plugins
│               ├── routes/          # API routes
│               └── services/        # Business logic
│
├── web-app/                         # Compose for Web Dashboard
│   ├── build.gradle.kts
│   └── src/
│       └── wasmJsMain/
│           └── kotlin/
│               ├── Main.kt
│               └── ui/
│
├── docker-compose.yml               # Local infrastructure
├── gradle.properties
├── settings.gradle.kts
└── build.gradle.kts
```

### 1.2 Root `settings.gradle.kts`

```kotlin
rootProject.name = "multilingual-system"

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

include(":shared")
include(":backend")
include(":web-app")
// include(":android-app")  // Optional
// include(":desktop-app")  // Optional
```

---

## 2. Backend: Ktor Server

### 2.1 Why Ktor over Spring Boot for KMP?

| Aspect | Ktor | Spring Boot |
|--------|------|-------------|
| **KMP Native** | ✅ Yes, pure Kotlin | ❌ JVM only |
| **Lightweight** | ✅ ~5MB | ❌ ~50MB+ |
| **Startup Time** | ✅ <1 second | ❌ 3-10 seconds |
| **Coroutines** | ✅ Native support | ⚠️ Reactor-based |
| **Code Sharing** | ✅ Share with clients | ❌ Limited |
| **Learning Curve** | ✅ Simple | ⚠️ Steeper |

### 2.2 Backend Dependencies (`backend/build.gradle.kts`)

```kotlin
plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("io.ktor.plugin") version "2.3.7"
}

application {
    mainClass.set("com.multilingual.ApplicationKt")
}

dependencies {
    // Shared KMP module
    implementation(project(":shared"))
    
    // Ktor Server
    implementation("io.ktor:ktor-server-core:2.3.7")
    implementation("io.ktor:ktor-server-netty:2.3.7")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
    implementation("io.ktor:ktor-server-cors:2.3.7")
    implementation("io.ktor:ktor-server-auth:2.3.7")
    implementation("io.ktor:ktor-server-auth-jwt:2.3.7")
    implementation("io.ktor:ktor-server-status-pages:2.3.7")
    
    // Database
    implementation("org.jetbrains.exposed:exposed-core:0.45.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.45.0")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:0.45.0")
    implementation("org.postgresql:postgresql:42.7.1")
    implementation("com.zaxxer:HikariCP:5.1.0")
    
    // Redis (optional for caching)
    implementation("io.lettuce:lettuce-core:6.3.0.RELEASE")
    
    // OpenAI
    implementation("com.aallam.openai:openai-client:3.6.2")
    
    // File parsing
    implementation("com.charleskorn.kaml:kaml:0.56.0")  // YAML
    
    // Logging
    implementation("ch.qos.logback:logback-classic:1.4.14")
    
    // Testing
    testImplementation("io.ktor:ktor-server-test-host:2.3.7")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}
```

### 2.3 Ktor Application Entry Point

```kotlin
// backend/src/main/kotlin/Application.kt
package com.multilingual

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.multilingual.plugins.*
import com.multilingual.routes.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configurePlugins()
        configureRouting()
    }.start(wait = true)
}

fun Application.configurePlugins() {
    configureSerialization()
    configureCORS()
    configureAuthentication()
    configureStatusPages()
    configureDatabase()
}

fun Application.configureRouting() {
    fileRoutes()
    translationRoutes()
    projectRoutes()
    downloadRoutes()
}
```

### 2.4 File Upload Route Example

```kotlin
// backend/src/main/kotlin/routes/FileRoutes.kt
package com.multilingual.routes

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.multilingual.shared.parser.FileParserFactory
import com.multilingual.services.FileStorageService
import java.io.File

fun Application.fileRoutes() {
    val storageService = FileStorageService()
    
    routing {
        route("/api/v1") {
            // Upload file
            post("/upload") {
                val multipart = call.receiveMultipart()
                var fileName = ""
                var fileBytes: ByteArray? = null
                var sourceLanguage = "en"
                
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
                
                fileBytes?.let { bytes ->
                    // Parse file using shared KMP parser
                    val parser = FileParserFactory.getParser(fileName)
                    val parseResult = parser.parse(bytes.decodeToString())
                    
                    // Store file locally
                    val fileId = storageService.saveFile(fileName, bytes)
                    
                    call.respond(HttpStatusCode.OK, mapOf(
                        "fileId" to fileId,
                        "fileName" to fileName,
                        "keyCount" to parseResult.keys.size,
                        "sourceLanguage" to sourceLanguage
                    ))
                } ?: call.respond(HttpStatusCode.BadRequest, "No file uploaded")
            }
            
            // Download generated file
            get("/download/{fileId}") {
                val fileId = call.parameters["fileId"] 
                    ?: return@get call.respond(HttpStatusCode.BadRequest)
                
                val file = storageService.getFile(fileId)
                if (file.exists()) {
                    call.response.header(
                        HttpHeaders.ContentDisposition,
                        ContentDisposition.Attachment.withParameter(
                            ContentDisposition.Parameters.FileName, 
                            file.name
                        ).toString()
                    )
                    call.respondFile(file)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }
    }
}
```

---

## 3. Shared KMP Module

### 3.1 Shared Module Configuration (`shared/build.gradle.kts`)

```kotlin
plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    // JVM target for backend
    jvm()
    
    // Web target (Compose for Web)
    wasmJs {
        browser()
    }
    
    // Optional: Mobile targets
    // androidTarget()
    // iosX64()
    // iosArm64()
    // iosSimulatorArm64()
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
            }
        }
        
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        
        val jvmMain by getting {
            dependencies {
                // JVM-specific XML parsing
                implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.16.0")
            }
        }
    }
}
```

### 3.2 Shared Data Models

```kotlin
// shared/src/commonMain/kotlin/models/Models.kt
package com.multilingual.shared.models

import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant

@Serializable
data class Project(
    val id: String,
    val name: String,
    val defaultLanguage: String,
    val createdAt: Instant
)

@Serializable
data class SourceFile(
    val id: String,
    val projectId: String,
    val fileName: String,
    val sourceLanguage: String,
    val format: FileFormat,
    val keyCount: Int,
    val uploadedAt: Instant
)

@Serializable
enum class FileFormat {
    XML,        // Android strings.xml
    JSON,       // Web i18n
    STRINGS,    // iOS Localizable.strings
    ARB,        // Flutter
    YAML,       // Various
    PROPERTIES  // Java
}

@Serializable
data class TranslationKey(
    val key: String,
    val value: String,
    val comment: String? = null,
    val plurals: Map<String, String>? = null
)

@Serializable
data class ParseResult(
    val keys: List<TranslationKey>,
    val format: FileFormat,
    val metadata: Map<String, String> = emptyMap()
)
```

### 3.3 Shared File Parsers (commonMain)

```kotlin
// shared/src/commonMain/kotlin/parser/FileParser.kt
package com.multilingual.shared.parser

import com.multilingual.shared.models.*

interface FileParser {
    fun parse(content: String): ParseResult
    fun generate(keys: List<TranslationKey>, language: String): String
}

object FileParserFactory {
    fun getParser(fileName: String): FileParser {
        return when {
            fileName.endsWith(".xml") -> XmlParser()
            fileName.endsWith(".json") || fileName.endsWith(".arb") -> JsonParser()
            fileName.endsWith(".strings") -> StringsParser()
            fileName.endsWith(".yaml") || fileName.endsWith(".yml") -> YamlParser()
            fileName.endsWith(".properties") -> PropertiesParser()
            else -> throw IllegalArgumentException("Unsupported format: $fileName")
        }
    }
}

// JSON Parser (works in commonMain)
class JsonParser : FileParser {
    override fun parse(content: String): ParseResult {
        val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
        val map = json.decodeFromString<Map<String, kotlinx.serialization.json.JsonElement>>(content)
        
        val keys = map.entries
            .filter { !it.key.startsWith("@") }
            .map { (key, value) ->
                TranslationKey(
                    key = key,
                    value = value.toString().trim('"'),
                    comment = map["@$key"]?.toString()
                )
            }
        
        return ParseResult(keys = keys, format = FileFormat.JSON)
    }
    
    override fun generate(keys: List<TranslationKey>, language: String): String {
        val entries = keys.joinToString(",\n  ") { key ->
            "\"${key.key}\": \"${key.value}\""
        }
        return "{\n  $entries\n}"
    }
}

// iOS .strings Parser
class StringsParser : FileParser {
    override fun parse(content: String): ParseResult {
        val regex = Regex("""(?:/\*\s*(.*?)\s*\*/\s*)?"([^"]+)"\s*=\s*"([^"]+)";""")
        val keys = regex.findAll(content).map { match ->
            TranslationKey(
                key = match.groupValues[2],
                value = match.groupValues[3],
                comment = match.groupValues[1].takeIf { it.isNotBlank() }
            )
        }.toList()
        
        return ParseResult(keys = keys, format = FileFormat.STRINGS)
    }
    
    override fun generate(keys: List<TranslationKey>, language: String): String {
        return keys.joinToString("\n\n") { key ->
            val comment = key.comment?.let { "/* $it */\n" } ?: ""
            "$comment\"${key.key}\" = \"${key.value}\";"
        }
    }
}
```

### 3.4 Shared API Client (for Web/Mobile)

```kotlin
// shared/src/commonMain/kotlin/api/ApiClient.kt
package com.multilingual.shared.api

import com.multilingual.shared.models.*
import kotlinx.serialization.Serializable

@Serializable
data class UploadResponse(
    val fileId: String,
    val fileName: String,
    val keyCount: Int,
    val sourceLanguage: String
)

@Serializable
data class GenerateRequest(
    val sourceFileId: String,
    val targetLanguages: List<String>,
    val outputFormats: Map<String, List<String>>
)

@Serializable
data class GenerateResponse(
    val generationId: String,
    val status: String,
    val files: List<GeneratedFile>
)

@Serializable
data class GeneratedFile(
    val language: String,
    val format: String,
    val downloadUrl: String,
    val keyCount: Int
)

// Expect/actual for platform-specific HTTP client
expect class HttpClient() {
    suspend fun uploadFile(file: ByteArray, fileName: String, language: String): UploadResponse
    suspend fun generateTranslations(request: GenerateRequest): GenerateResponse
    suspend fun downloadFile(url: String): ByteArray
}
```

---

## 4. Web Dashboard: Compose for Web

### 4.1 Why Compose for Web?

| Benefit | Description |
|---------|-------------|
| **Code Sharing** | Share UI components with Android/Desktop |
| **Type Safety** | Kotlin compile-time checks |
| **Familiar** | Same Compose API as Android |
| **Single Language** | All Kotlin, no JavaScript |

### 4.2 Web App Configuration (`web-app/build.gradle.kts`)

```kotlin
plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose") version "1.5.11"
}

kotlin {
    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "multilingual-web.js"
            }
        }
        binaries.executable()
    }
    
    sourceSets {
        val wasmJsMain by getting {
            dependencies {
                implementation(project(":shared"))
                implementation(compose.runtime)
                implementation(compose.ui)
                implementation(compose.foundation)
                implementation(compose.material3)
                
                // Ktor client for WASM
                implementation("io.ktor:ktor-client-core:2.3.7")
                implementation("io.ktor:ktor-client-js:2.3.7")
            }
        }
    }
}

compose {
    experimental {
        web.application {}
    }
}
```

### 4.3 Compose Web UI Components

```kotlin
// web-app/src/wasmJsMain/kotlin/Main.kt
package com.multilingual.web

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.multilingual.web.ui.App

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow(canvasElementId = "composeTarget") {
        App()
    }
}
```

```kotlin
// web-app/src/wasmJsMain/kotlin/ui/App.kt
package com.multilingual.web.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun App() {
    var currentScreen by remember { mutableStateOf(Screen.Upload) }
    
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column {
                // Navigation
                NavigationBar(currentScreen) { currentScreen = it }
                
                // Content
                when (currentScreen) {
                    Screen.Upload -> UploadScreen()
                    Screen.History -> HistoryScreen()
                    Screen.Settings -> SettingsScreen()
                }
            }
        }
    }
}

enum class Screen { Upload, History, Settings }

@Composable
fun UploadScreen() {
    var uploadedFile by remember { mutableStateOf<String?>(null) }
    var selectedLanguages by remember { mutableStateOf(setOf<String>()) }
    var isGenerating by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Left: File Upload Area
        Card(
            modifier = Modifier.weight(1f).fillMaxHeight()
        ) {
            FileDropZone(
                onFileSelected = { fileName ->
                    uploadedFile = fileName
                }
            )
        }
        
        // Right: Configuration Panel
        Card(
            modifier = Modifier.weight(1f).fillMaxHeight()
        ) {
            ConfigurationPanel(
                uploadedFile = uploadedFile,
                selectedLanguages = selectedLanguages,
                onLanguageToggle = { lang ->
                    selectedLanguages = if (lang in selectedLanguages) {
                        selectedLanguages - lang
                    } else {
                        selectedLanguages + lang
                    }
                },
                onGenerate = {
                    isGenerating = true
                    // Call API...
                }
            )
        }
    }
}

@Composable
fun FileDropZone(onFileSelected: (String) -> Unit) {
    // Drag & drop implementation
    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📁", style = MaterialTheme.typography.displayLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Drop your localization file here")
            Text("or", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { /* Open file picker */ }) {
                Text("Browse Files")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Supported: .xml, .json, .strings, .arb, .yaml",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ConfigurationPanel(
    uploadedFile: String?,
    selectedLanguages: Set<String>,
    onLanguageToggle: (String) -> Unit,
    onGenerate: () -> Unit
) {
    val languages = listOf(
        "zh-CN" to "Chinese (Simplified)",
        "ja" to "Japanese",
        "ko" to "Korean",
        "es" to "Spanish",
        "fr" to "French",
        "de" to "German"
    )
    
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Generation Settings", style = MaterialTheme.typography.titleLarge)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        uploadedFile?.let {
            Text("📁 Source: $it", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        Text("Target Languages:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        
        languages.forEach { (code, name) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Checkbox(
                    checked = code in selectedLanguages,
                    onCheckedChange = { onLanguageToggle(code) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(name)
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = onGenerate,
            enabled = uploadedFile != null && selectedLanguages.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🚀 Generate Translations")
        }
    }
}
```

---

## 5. Local Infrastructure (Docker Compose)

### 5.1 `docker-compose.yml`

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    container_name: multilingual-db
    environment:
      POSTGRES_DB: multilingual
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 5s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7-alpine
    container_name: multilingual-redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    command: redis-server --appendonly yes

  # Optional: Adminer for DB management
  adminer:
    image: adminer
    container_name: multilingual-adminer
    ports:
      - "8081:8080"
    depends_on:
      - postgres

volumes:
  postgres_data:
  redis_data:
```

### 5.2 Database Init Script (`init.sql`)

```sql
-- init.sql
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE projects (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    default_language VARCHAR(10) NOT NULL DEFAULT 'en',
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE source_files (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id UUID REFERENCES projects(id),
    file_name VARCHAR(255) NOT NULL,
    source_language VARCHAR(10) NOT NULL,
    format VARCHAR(20) NOT NULL,
    key_count INTEGER DEFAULT 0,
    file_path VARCHAR(500),
    uploaded_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE translations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    source_file_id UUID REFERENCES source_files(id),
    target_language VARCHAR(10) NOT NULL,
    key VARCHAR(255) NOT NULL,
    value TEXT NOT NULL,
    status VARCHAR(20) DEFAULT 'pending',
    confidence DECIMAL(3,2),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(source_file_id, target_language, key)
);

CREATE TABLE generation_jobs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    source_file_id UUID REFERENCES source_files(id),
    target_languages TEXT[] NOT NULL,
    output_formats JSONB NOT NULL,
    status VARCHAR(20) DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT NOW(),
    completed_at TIMESTAMP
);

CREATE INDEX idx_translations_source_file ON translations(source_file_id);
CREATE INDEX idx_translations_language ON translations(target_language);
```

---

## 6. Translation Service (OpenAI Integration)

### 6.1 OpenAI Translation Service

```kotlin
// backend/src/main/kotlin/services/TranslationService.kt
package com.multilingual.services

import com.aallam.openai.api.chat.*
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.multilingual.shared.models.TranslationKey
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class TranslationService(
    private val openAI: OpenAI
) {
    suspend fun translateBatch(
        keys: List<TranslationKey>,
        sourceLanguage: String,
        targetLanguage: String
    ): List<TranslationKey> = coroutineScope {
        // Process in chunks of 20 for efficiency
        keys.chunked(20).flatMap { chunk ->
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
        val prompt = buildPrompt(key.value, sourceLanguage, targetLanguage, key.comment)
        
        val response = openAI.chatCompletion(
            ChatCompletionRequest(
                model = ModelId("gpt-4-turbo"),
                messages = listOf(
                    ChatMessage(
                        role = ChatRole.System,
                        content = """You are a professional translator specializing in mobile app and web UI localization.
                            |Rules:
                            |1. Preserve all placeholders like %s, %d, %1${'$'}s, {name}, {{count}}
                            |2. Keep the translation concise and natural
                            |3. Match the tone and formality of the source
                            |4. Return ONLY the translated text, no explanations""".trimMargin()
                    ),
                    ChatMessage(
                        role = ChatRole.User,
                        content = prompt
                    )
                ),
                temperature = 0.3
            )
        )
        
        val translatedValue = response.choices.firstOrNull()?.message?.content ?: key.value
        
        return key.copy(value = translatedValue)
    }
    
    private fun buildPrompt(
        text: String,
        sourceLanguage: String,
        targetLanguage: String,
        context: String?
    ): String {
        val contextHint = context?.let { "\nContext: $it" } ?: ""
        return """Translate from $sourceLanguage to $targetLanguage:$contextHint
            |
            |"$text"""".trimMargin()
    }
}
```

---

## 7. Local File Storage Service

```kotlin
// backend/src/main/kotlin/services/FileStorageService.kt
package com.multilingual.services

import java.io.File
import java.util.UUID

class FileStorageService(
    private val baseDir: String = System.getProperty("user.home") + "/multilingual-files"
) {
    init {
        File(baseDir).mkdirs()
        File("$baseDir/uploads").mkdirs()
        File("$baseDir/generated").mkdirs()
    }
    
    fun saveFile(fileName: String, content: ByteArray): String {
        val fileId = UUID.randomUUID().toString()
        val extension = fileName.substringAfterLast('.')
        val file = File("$baseDir/uploads/$fileId.$extension")
        file.writeBytes(content)
        return fileId
    }
    
    fun getFile(fileId: String): File {
        val uploadsDir = File("$baseDir/uploads")
        return uploadsDir.listFiles()?.find { it.name.startsWith(fileId) }
            ?: File("$baseDir/generated").listFiles()?.find { it.name.startsWith(fileId) }
            ?: throw NoSuchFileException(File(fileId))
    }
    
    fun saveGeneratedFile(
        generationId: String,
        language: String,
        format: String,
        content: String
    ): String {
        val dir = File("$baseDir/generated/$generationId/$language")
        dir.mkdirs()
        
        val fileName = "strings.$format"
        val file = File(dir, fileName)
        file.writeText(content)
        
        return "$generationId/$language/$fileName"
    }
    
    fun createZipArchive(generationId: String): File {
        val generatedDir = File("$baseDir/generated/$generationId")
        val zipFile = File("$baseDir/generated/$generationId.zip")
        
        // Create ZIP (use java.util.zip)
        java.util.zip.ZipOutputStream(zipFile.outputStream()).use { zip ->
            generatedDir.walkTopDown().forEach { file ->
                if (file.isFile) {
                    val entry = java.util.zip.ZipEntry(
                        file.relativeTo(generatedDir).path
                    )
                    zip.putNextEntry(entry)
                    file.inputStream().copyTo(zip)
                    zip.closeEntry()
                }
            }
        }
        
        return zipFile
    }
}
```

---

## 8. Quick Start Commands (macOS)

### 8.1 Prerequisites

```bash
# Install Homebrew (if not installed)
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Install required tools
brew install openjdk@21
brew install gradle
brew install docker
brew install --cask docker  # Docker Desktop

# Set JAVA_HOME
echo 'export JAVA_HOME=$(/usr/libexec/java_home -v 21)' >> ~/.zshrc
source ~/.zshrc
```

### 8.2 Start Development

```bash
# Clone/create project
mkdir multilingual-system && cd multilingual-system

# Start infrastructure
docker-compose up -d

# Verify services
docker-compose ps
# postgres should be running on :5432
# redis should be running on :6379

# Run backend
cd backend
./gradlew run
# Server starts at http://localhost:8080

# Run web app (in another terminal)
cd web-app
./gradlew wasmJsBrowserDevelopmentRun
# Opens browser at http://localhost:8080
```

### 8.3 Environment Configuration

```bash
# backend/src/main/resources/application.conf
ktor {
    deployment {
        port = 8080
    }
    application {
        modules = [ com.multilingual.ApplicationKt.module ]
    }
}

database {
    url = "jdbc:postgresql://localhost:5432/multilingual"
    user = "postgres"
    password = "postgres"
}

redis {
    host = "localhost"
    port = 6379
}

openai {
    apiKey = ${?OPENAI_API_KEY}
}

storage {
    basePath = ${user.home}"/multilingual-files"
}
```

```bash
# Set OpenAI API key
export OPENAI_API_KEY="sk-your-key-here"
```

---

## 9. Cost Estimation (Local Development)

| Item | Cost |
|------|------|
| **Infrastructure** | $0 (Docker on MacBook) |
| **OpenAI API** | ~$0.01-0.03 per 1K tokens |
| **Typical translation job** | ~$0.10-0.50 |
| **Monthly (light usage)** | ~$5-20 |

### Cost Optimization Tips

1. **Use GPT-3.5-turbo** for simple strings (~10x cheaper)
2. **Cache translations** in PostgreSQL/Redis
3. **Batch API calls** to reduce overhead
4. **Use DeepL free tier** for European languages (500K chars/month free)

---

## 10. Summary: KMP-Focused Local Stack

| Component | Technology | Reason |
|-----------|------------|--------|
| **Shared Logic** | Kotlin Multiplatform | Code reuse across all platforms |
| **Backend** | Ktor | Lightweight, KMP-native, coroutines |
| **Web UI** | Compose for Web | Same UI code as Android |
| **Database** | PostgreSQL | Robust, JSON support |
| **Cache** | Redis | Fast, simple |
| **Storage** | Local filesystem | Simple for dev |
| **AI** | OpenAI GPT-4 | Best quality translations |
| **Build** | Gradle + KMP plugin | Native KMP support |
| **Infra** | Docker Compose | Easy local setup |

---

**Document Version:** 2.0 (KMP Local Edition)  
**Last Updated:** December 18, 2025  
**Target Environment:** macOS Local Development
