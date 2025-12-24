package com.epam.test.multilingualgenerationsystem.translation

class TranslationEngine {
    fun translate(key: String, targetLanguage: String): String {
        return "[$targetLanguage] $key"
    }
}
