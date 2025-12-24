package com.epam.test.multilingualgenerationsystem.models

import kotlinx.serialization.Serializable

@Serializable
data class GenerateRequest(
    val sourceFileId: String,
    val targetLanguages: List<String>,
    val outputFormats: Map<String, List<FileFormat>> = emptyMap()
)
