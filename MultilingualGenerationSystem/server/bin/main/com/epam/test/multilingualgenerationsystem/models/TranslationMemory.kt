package com.epam.test.multilingualgenerationsystem.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object TranslationMemory : Table("translation_memory") {
    val id = uuid("id").autoGenerate()
    val sourceLanguage = varchar("source_language", 10)
    val targetLanguage = varchar("target_language", 10)
    val sourceText = text("source_text")
    val translatedText = text("translated_text")
    val context = varchar("context", 255).nullable()
    val confidenceScore = decimal("confidence_score", 3, 2).nullable()
    val usageCount = integer("usage_count").default(1)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}
