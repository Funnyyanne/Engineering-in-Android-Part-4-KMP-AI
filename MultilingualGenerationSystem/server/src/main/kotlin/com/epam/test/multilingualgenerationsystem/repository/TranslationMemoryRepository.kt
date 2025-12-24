package com.epam.test.multilingualgenerationsystem.repository

import com.epam.test.multilingualgenerationsystem.models.TranslationMemory
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class TranslationMemoryRepository {

    init {
        transaction {
            SchemaUtils.create(TranslationMemory)
        }
    }

    fun findTranslation(sourceText: String, sourceLang: String, targetLang: String): String? {
        return transaction {
            TranslationMemory.select {
                (TranslationMemory.sourceText eq sourceText) and
                (TranslationMemory.sourceLanguage eq sourceLang) and
                (TranslationMemory.targetLanguage eq targetLang)
            }.map { it[TranslationMemory.translatedText] }
             .firstOrNull()
        }
    }

    fun saveTranslation(sourceText: String, translatedText: String, sourceLang: String, targetLang: String) {
        transaction {
            // Check if it already exists to avoid duplicates (could also use unique index/upsert)
            val exists = TranslationMemory.select {
                (TranslationMemory.sourceText eq sourceText) and
                (TranslationMemory.sourceLanguage eq sourceLang) and
                (TranslationMemory.targetLanguage eq targetLang)
            }.count() > 0

            if (!exists) {
                TranslationMemory.insert {
                    it[this.sourceText] = sourceText
                    it[this.translatedText] = translatedText
                    it[this.sourceLanguage] = sourceLang
                    it[this.targetLanguage] = targetLang
                }
            } else {
                // Optionally update usage count or last updated time
                TranslationMemory.update({
                    (TranslationMemory.sourceText eq sourceText) and
                    (TranslationMemory.sourceLanguage eq sourceLang) and
                    (TranslationMemory.targetLanguage eq targetLang)
                }) {
                    with(SqlExpressionBuilder) {
                        it.update(usageCount, usageCount + 1)
                    }
                }
            }
        }
    }
}
