package com.epam.test.multilingualgenerationsystem.parser

import com.epam.test.multilingualgenerationsystem.models.FileFormat
import com.epam.test.multilingualgenerationsystem.models.ParseResult
import com.epam.test.multilingualgenerationsystem.models.TranslationKey
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.decodeFromString

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
            fileName.endsWith(".yaml") || fileName.endsWith(".yml") -> throw NotImplementedError("YAML parser not implemented")
            fileName.endsWith(".properties") -> PropertiesParser()
            fileName.endsWith(".txt") -> PlainTextParser()
            else -> throw IllegalArgumentException("Unsupported format: $fileName")
        }
    }
}

class StubParser : FileParser {
    override fun parse(content: String): ParseResult {
        return ParseResult(keys = emptyList(), format = FileFormat.XML)
    }

    override fun generate(keys: List<TranslationKey>, language: String): String {
        return ""
    }
}

// JSON Parser (works in commonMain)
class JsonParser : FileParser {
    override fun parse(content: String): ParseResult {
        val json = Json { ignoreUnknownKeys = true }
        val map = json.decodeFromString<Map<String, JsonElement>>(content)
        
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

class PlainTextParser : FileParser {
    override fun parse(content: String): ParseResult {
        return ParseResult(
            keys = listOf(TranslationKey("content", content)),
            format = FileFormat.TXT
        )
    }

    override fun generate(keys: List<TranslationKey>, language: String): String {
        return keys.joinToString("\n") { it.value }
    }
}

class XmlParser : FileParser {
    override fun parse(content: String): ParseResult {
        val keys = mutableListOf<TranslationKey>()
        val regex = Regex("""<string name="([^"]+)">([^<]+)</string>""")
        regex.findAll(content).forEach { matchResult ->
            keys.add(TranslationKey(matchResult.groupValues[1], matchResult.groupValues[2]))
        }
        return ParseResult(keys, FileFormat.XML)
    }

    override fun generate(keys: List<TranslationKey>, language: String): String {
        val sb = StringBuilder()
        sb.append("<resources>\n")
        keys.forEach { key ->
            sb.append("    <string name=\"${key.key}\">${key.value}</string>\n")
        }
        sb.append("</resources>")
        return sb.toString()
    }
}

class StringsParser : FileParser {
    override fun parse(content: String): ParseResult {
        val keys = mutableListOf<TranslationKey>()
        // "key" = "value";
        val regex = Regex(""""([^"]+)"\s*=\s*"([^"]+)";""")
        regex.findAll(content).forEach { match ->
            keys.add(TranslationKey(match.groupValues[1], match.groupValues[2]))
        }
        return ParseResult(keys, FileFormat.STRINGS)
    }

    override fun generate(keys: List<TranslationKey>, language: String): String {
        return keys.joinToString("\n") { "\"${it.key}\" = \"${it.value}\";" }
    }
}

class PropertiesParser : FileParser {
    override fun parse(content: String): ParseResult {
        val keys = content.lines().mapNotNull { line ->
            val parts = line.split("=", limit = 2)
            if (parts.size == 2) {
                TranslationKey(parts[0].trim(), parts[1].trim())
            } else null
        }
        return ParseResult(keys, FileFormat.PROPERTIES)
    }

    override fun generate(keys: List<TranslationKey>, language: String): String {
        return keys.joinToString("\n") { "${it.key}=${it.value}" }
    }
}
