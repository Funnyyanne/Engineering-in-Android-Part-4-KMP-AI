package com.epam.test.multilingualgenerationsystem.parser

import com.epam.test.multilingualgenerationsystem.models.FileFormat
import com.epam.test.multilingualgenerationsystem.models.TranslationKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class JsonParserTest {

    @Test
    fun `parse json content extracts keys and values`() {
        val jsonContent = """
            {
                "app_name": "My Application",
                "@app_name": {
                    "description": "Name of the application"
                },
                "welcome_message": "Welcome, %1${'$'}s!"
            }
        """.trimIndent()

        val parser = JsonParser()
        val result = parser.parse(jsonContent)

        assertEquals(FileFormat.JSON, result.format)
        assertEquals(2, result.keys.size)

        val appNameKey = result.keys.find { it.key == "app_name" }
        assertNotNull(appNameKey)
        assertEquals("My Application", appNameKey.value)
        assertEquals("""{"description":"Name of the application"}""", appNameKey.comment)

        val welcomeMessageKey = result.keys.find { it.key == "welcome_message" }
        assertNotNull(welcomeMessageKey)
        assertEquals("Welcome, %1${'$'}s!", welcomeMessageKey.value)
    }

    @Test
    fun `generate json content produces correct string`() {
        val keys = listOf(
            TranslationKey("app_name", "My App"),
            TranslationKey("welcome", "Hello")
        )

        val parser = JsonParser()
        val generatedJson = parser.generate(keys, "en")

        val expectedJson = """
            {
              "app_name": "My App",
              "welcome": "Hello"
            }
        """.trimIndent()

        assertEquals(expectedJson, generatedJson)
    }
}
