package com.epam.test.multilingualgenerationsystem

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "MultilingualGenerationSystem",
    ) {
        App()
    }
}