package com.epam.test.multilingualgenerationsystem.generator

import com.epam.test.multilingualgenerationsystem.models.FileFormat
import com.epam.test.multilingualgenerationsystem.models.TranslationKey

interface FileGenerator {
    fun generate(language: String, keys: List<TranslationKey>): String
}

object FileGeneratorFactory {
    fun getGenerator(format: FileFormat): FileGenerator {
        return when (format) {
            FileFormat.XML -> XmlGenerator()
            FileFormat.JSON, FileFormat.ARB -> JsonGenerator()
            FileFormat.STRINGS -> StringsGenerator()
            FileFormat.PROPERTIES -> PropertiesGenerator()
            FileFormat.TXT -> PlainTextGenerator()
            else -> throw NotImplementedError("Generator for $format not implemented")
        }
    }
}

class XmlGenerator : FileGenerator {
    override fun generate(language: String, keys: List<TranslationKey>): String {
        val body = keys.joinToString("\n") { key -> "    <string name=\"${key.key}\">${key.value}</string>" }
        return "<resources>\n$body\n</resources>"
    }
}

class JsonGenerator : FileGenerator {
    override fun generate(language: String, keys: List<TranslationKey>): String {
        val entries = keys.joinToString(",\n  ") { key ->
            "\"${key.key}\": \"${key.value}\""
        }
        return "{\n  $entries\n}"
    }
}

class StringsGenerator : FileGenerator {
    override fun generate(language: String, keys: List<TranslationKey>): String {
        return keys.joinToString("\n") { key ->
            "\"${key.key}\" = \"${key.value}\";"
        }
    }
}

class PropertiesGenerator : FileGenerator {
    override fun generate(language: String, keys: List<TranslationKey>): String {
        return keys.joinToString("\n") { key ->
            "${key.key}=${key.value}"
        }
    }
}

class PlainTextGenerator : FileGenerator {
    override fun generate(language: String, keys: List<TranslationKey>): String {
        return keys.joinToString("\n") { it.value }
    }
}