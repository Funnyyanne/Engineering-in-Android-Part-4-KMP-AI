package com.epam.test.multilingualgenerationsystem.models

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
    PROPERTIES, // Java
    TXT         // Plain text
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
