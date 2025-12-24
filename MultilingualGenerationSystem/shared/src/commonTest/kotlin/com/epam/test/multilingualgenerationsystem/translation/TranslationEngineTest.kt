package com.epam.test.multilingualgenerationsystem.translation

import kotlin.test.Test
import kotlin.test.assertEquals

class TranslationEngineTest {

    @Test
    fun `translation engine stub prefixes language`() {
        val engine = TranslationEngine()
        assertEquals("[fr] hello", engine.translate("hello", "fr"))
        assertEquals("[es] world", engine.translate("world", "es"))
    }
}
