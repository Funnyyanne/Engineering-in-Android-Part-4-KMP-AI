package com.epam.test.multilingualgenerationsystem

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import compose.icons.FeatherIcons
import compose.icons.feathericons.Download
import compose.icons.feathericons.FileText
import compose.icons.feathericons.Send
import compose.icons.feathericons.Upload
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.epam.test.multilingualgenerationsystem.models.FileFormat
import com.epam.test.multilingualgenerationsystem.models.GenerateRequest
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    var currentScreen by remember { mutableStateOf(Screen.Upload) }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column {
                // Navigation
                // NavigationBar(currentScreen) { currentScreen = it } // Will implement later

                // Content
                when (currentScreen) {
                    Screen.Upload -> UploadScreen()
                    Screen.History -> HistoryScreen()
                    Screen.Settings -> SettingsScreen()
                }
            }
        }
    }
}

enum class Screen { Upload, History, Settings }

@Composable
fun UploadScreen() {
    var uploadedFile by remember { mutableStateOf<String?>(null) }
    var uploadedFileId by remember { mutableStateOf<String?>(null) }
    var selectedLanguages by remember { mutableStateOf(setOf<String>()) }
    var selectedFormats by remember { mutableStateOf(mapOf<String, List<FileFormat>>()) }
    var isGenerating by remember { mutableStateOf(false) }
    var downloadId by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    val client = remember {
        HttpClient {
            install(ContentNegotiation) {
                json()
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: File Upload Area
        Card(
            modifier = Modifier.weight(1f).fillMaxHeight()
        ) {
            FileDropZone(
                onFileSelected = { fileName, fileContent ->
                    uploadedFile = fileName
                    downloadId = null
                    uploadedFileId = null // Reset ID
                    
                    if (fileContent != null) {
                        scope.launch {
                            try {
                                val response = client.submitFormWithBinaryData(
                                    url = "http://localhost:8080/api/v1/upload",
                                    formData = formData {
                                        append("file", fileContent, Headers.build {
                                            append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                                        })
                                        append("sourceLanguage", "en")
                                    }
                                )
                                if (response.status == HttpStatusCode.OK) {
                                    val body: Map<String, String> = response.body()
                                    uploadedFileId = body["fileId"]
                                    println("File uploaded successfully. ID: $uploadedFileId")
                                } else {
                                    println("Upload failed: ${response.status}")
                                }
                            } catch (e: Exception) {
                                println("Upload error: ${e.message}")
                                e.printStackTrace()
                            }
                        }
                    }
                }
            )
        }

        // Right: Configuration Panel
        Card(
            modifier = Modifier.weight(1f).fillMaxHeight()
        ) {
            ConfigurationPanel(
                uploadedFile = uploadedFile,
                uploadedFileId = uploadedFileId,
                selectedLanguages = selectedLanguages,
                selectedFormats = selectedFormats,
                isGenerating = isGenerating,
                downloadId = downloadId,
                onLanguageToggle = { lang ->
                    selectedLanguages = if (lang in selectedLanguages) {
                        selectedLanguages - lang
                    } else {
                        selectedLanguages + lang
                    }
                    if (lang !in selectedFormats && lang in selectedLanguages) {
                        selectedFormats = selectedFormats + (lang to listOf(FileFormat.JSON))
                    }
                },
                onFormatToggle = { lang, format ->
                    val current = selectedFormats[lang] ?: emptyList()
                    val next = if (format in current) current - format else current + format
                    selectedFormats = selectedFormats + (lang to next)
                },
                onGenerate = {
                    if (uploadedFileId == null) {
                        println("Cannot generate: File not uploaded yet.")
                        return@ConfigurationPanel
                    }
                    scope.launch {
                        isGenerating = true
                        try {
                            val response = client.post("http://localhost:8080/api/v1/generate") {
                                contentType(ContentType.Application.Json)
                                setBody(
                                    GenerateRequest(
                                        sourceFileId = uploadedFileId!!,
                                        targetLanguages = selectedLanguages.toList(),
                                        outputFormats = selectedFormats
                                    )
                                )
                            }
                            if (response.status == HttpStatusCode.OK) {
                                val body: Map<String, String> = response.body()
                                downloadId = body["generationId"]
                            } else {
                                println("Generation request failed: ${response.status} - ${response.body<String>()}")
                            }
                        } catch (e: Exception) {
                            println("Generation failed: ${e.message}")
                            e.printStackTrace()
                        } finally {
                            isGenerating = false
                        }
                    }
                },
                onDownload = {
                    downloadId?.let { id ->
                        downloadFile("http://localhost:8080/api/v1/download/$id", "$id.zip")
                    }
                }
            )
        }
    }
}

@Composable
fun FileDropZone(onFileSelected: (String, ByteArray?) -> Unit) {
    // Drag & drop implementation
    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = FeatherIcons.Upload,
                contentDescription = "Upload",
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Drop your localization file here")
            Text("or", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                openFilePicker { fileName, fileContent ->
                    onFileSelected(fileName, fileContent)
                }
            }) {
                Text("Browse Files")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Supported: .xml, .json, .strings, .arb, .yaml",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ConfigurationPanel(
    uploadedFile: String?,
    uploadedFileId: String?,
    selectedLanguages: Set<String>,
    selectedFormats: Map<String, List<FileFormat>>,
    isGenerating: Boolean,
    downloadId: String?,
    onLanguageToggle: (String) -> Unit,
    onFormatToggle: (String, FileFormat) -> Unit,
    onGenerate: () -> Unit,
    onDownload: () -> Unit
) {
    val languages = listOf(
        "en" to "English",
        "zh-CN" to "Chinese (Simplified)",
        "ja" to "Japanese",
        "ko" to "Korean",
        "es" to "Spanish",
        "fr" to "French",
        "de" to "German"
    )

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Generation Settings", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        uploadedFile?.let {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = FeatherIcons.FileText,
                    contentDescription = "Source file",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Source: $it", style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text("Target Languages & Formats:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        languages.forEach { (code, name) ->
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = code in selectedLanguages,
                        onCheckedChange = { onLanguageToggle(code) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(name)
                }
                if (code in selectedLanguages) {
                    Row(modifier = Modifier.padding(start = 32.dp)) {
                        listOf(FileFormat.JSON, FileFormat.XML, FileFormat.STRINGS).forEach { format ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 8.dp)) {
                                FilterChip(
                                    selected = format in (selectedFormats[code] ?: emptyList()),
                                    onClick = { onFormatToggle(code, format) },
                                    label = { Text(format.name) }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        if (downloadId != null && !isGenerating) {
            Button(
                onClick = onDownload,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Icon(
                    imageVector = FeatherIcons.Download,
                    contentDescription = "Download result",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Download Result ZIP")
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = onGenerate,
            enabled = uploadedFileId != null && selectedLanguages.isNotEmpty() && !isGenerating,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isGenerating) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generating...")
            } else {
                Icon(
                    imageVector = FeatherIcons.Send,
                    contentDescription = "Generate translations",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate Translations")
            }
        }
        if (uploadedFile != null && uploadedFileId == null) {
            Text("Uploading...", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
fun HistoryScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("History Screen (Not Implemented)")
    }
}

@Composable
fun SettingsScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Settings Screen (Not Implemented)")
    }
}