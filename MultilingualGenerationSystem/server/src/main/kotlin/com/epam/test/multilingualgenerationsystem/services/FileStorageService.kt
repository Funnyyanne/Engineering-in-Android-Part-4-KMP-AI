package com.epam.test.multilingualgenerationsystem.services

import java.io.File
import java.util.UUID

class FileStorageService(
    private val baseDir: String = System.getProperty("user.home") + "/multilingual-system/uploads"
) {
    init {
        File(baseDir).mkdirs()
    }
    
    fun saveFile(fileName: String, content: ByteArray): String {
        val fileId = UUID.randomUUID().toString()
        val extension = fileName.substringAfterLast('.', "")
        val safeFileName = if (extension.isNotEmpty()) "$fileId.$extension" else fileId
        val file = File(baseDir, safeFileName)
        file.writeBytes(content)
        return fileId
    }
    
    fun getFile(fileId: String): File? {
        val dir = File(baseDir)
        return dir.listFiles()?.find { it.name.startsWith(fileId) }
    }
}
