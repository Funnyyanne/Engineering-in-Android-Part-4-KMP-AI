package com.epam.test.multilingualgenerationsystem.services

import com.epam.test.multilingualgenerationsystem.generator.FileGeneratorFactory
import com.epam.test.multilingualgenerationsystem.models.FileFormat
import com.epam.test.multilingualgenerationsystem.models.TranslationKey
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.io.File
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class BatchTranslationService(
    private val translationService: TranslationService,
    private val baseDir: String = System.getProperty("user.home") + "/multilingual-system/generated-translations"
) {
    init {
        val dir = File(baseDir)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        println("BatchTranslationService initialized. Base dir: ${dir.absolutePath}")
    }

    suspend fun processBatch(
        keys: List<TranslationKey>,
        sourceLanguage: String,
        targetLanguages: List<String>,
        outputFormats: Map<String, List<FileFormat>>
    ): String = coroutineScope {
        val generationId = UUID.randomUUID().toString()
        val generationDir = File(baseDir, generationId)
        generationDir.mkdirs()
        println("Started processing batch. Generation ID: $generationId. Dir: ${generationDir.absolutePath}")

        targetLanguages.map { targetLang ->
            async {
                val translatedKeys = translationService.translateBatch(keys, sourceLanguage, targetLang)
                val formats = outputFormats[targetLang] ?: listOf(FileFormat.JSON)
                
                formats.forEach { format ->
                    val generator = FileGeneratorFactory.getGenerator(format)
                    val content = generator.generate(targetLang, translatedKeys)
                    val langDir = File(generationDir, targetLang)
                    langDir.mkdirs()
                    val fileName = "strings.${format.name.lowercase()}"
                    File(langDir, fileName).writeText(content)
                }
            }
        }.awaitAll()

        val zipFile = File(baseDir, "$generationId.zip")
        createZipArchive(generationDir, zipFile)
        println("Created zip archive: ${zipFile.absolutePath}")
        generationId
    }

    private fun createZipArchive(sourceDir: File, zipFile: File) {
        val fos = zipFile.outputStream()
        ZipOutputStream(fos).use { zip ->
            sourceDir.walkTopDown().forEach { file ->
                if (file.isFile) {
                    val relativePath = file.relativeTo(sourceDir).path
                    val entry = ZipEntry(relativePath)
                    zip.putNextEntry(entry)
                    file.inputStream().use { input ->
                        input.copyTo(zip)
                    }
                    zip.closeEntry()
                }
            }
        }
    }

    fun getZipFile(generationId: String): File {
        val file = File(baseDir, "$generationId.zip")
        println("Requested zip file: ${file.absolutePath}. Exists: ${file.exists()}")
        return file
    }
}
