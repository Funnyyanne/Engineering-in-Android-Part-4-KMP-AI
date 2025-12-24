package com.epam.test.multilingualgenerationsystem.generator

import com.epam.test.multilingualgenerationsystem.models.FileFormat
import com.epam.test.multilingualgenerationsystem.models.TranslationKey
import kotlin.test.Test
import kotlin.test.assertEquals

class FileGeneratorFactoryTest {

    private val testKeys = listOf(
        TranslationKey("key1", "value1"),
        TranslationKey("key2", "value2")
    )

    @Test
    fun `xml generator produces correct output`() {
        val generator = FileGeneratorFactory.getGenerator(FileFormat.XML)
        val output = generator.generate("en", testKeys)
        val expected = """
            <resources>
                <string name="key1">value1</string>
                <string name="key2">value2</string>
            </resources>
        """.trimIndent()
        assertEquals(expected, output)
    }

    @Test
    fun `json generator produces correct output`() {
        val generator = FileGeneratorFactory.getGenerator(FileFormat.JSON)
        val output = generator.generate("en", testKeys)
        val expected = """
            {
              "key1": "value1",
              "key2": "value2"
            }
        """.trimIndent()
        assertEquals(expected, output)
    }

    @Test
    fun `strings generator produces correct output`() {
        val generator = FileGeneratorFactory.getGenerator(FileFormat.STRINGS)
        val output = generator.generate("en", testKeys)
        val expected = """
            "key1" = "value1";
            "key2" = "value2";
        """.trimIndent()
        assertEquals(expected, output)
    }

    @Test
    fun `properties generator produces correct output`() {
        val generator = FileGeneratorFactory.getGenerator(FileFormat.PROPERTIES)
        val output = generator.generate("en", testKeys)
        val expected = """
            key1=value1
            key2=value2
        """.trimIndent()
        assertEquals(expected, output)
    }
}
