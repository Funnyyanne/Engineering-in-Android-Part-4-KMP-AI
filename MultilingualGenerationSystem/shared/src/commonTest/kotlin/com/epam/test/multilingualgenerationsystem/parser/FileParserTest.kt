package com.epam.test.multilingualgenerationsystem.parser

import com.epam.test.multilingualgenerationsystem.models.FileFormat
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class FileParserTest {

    @Test
    fun `stub parser returns empty key set`() {
        val parser = StubParser()
        val result = parser.parse("")
        assertTrue(result.keys.isEmpty())
        assertEquals(FileFormat.XML, result.format)
    }

    @Test
    fun `file parser factory throws not implemented for supported types`() {
        assertFailsWith<NotImplementedError> {
            FileParserFactory.getParser("strings.xml")
        }
        assertFailsWith<NotImplementedError> {
            FileParserFactory.getParser("Localizable.strings")
        }
        assertFailsWith<NotImplementedError> {
            FileParserFactory.getParser("config.yaml")
        }
        assertFailsWith<NotImplementedError> {
            FileParserFactory.getParser("config.yml")
        }
        assertFailsWith<NotImplementedError> {
            FileParserFactory.getParser("messages.properties")
        }
    }

    @Test
    fun `file parser factory throws illegal argument for unsupported types`() {
        assertFailsWith<IllegalArgumentException> {
            FileParserFactory.getParser("document.txt")
        }
    }
}
