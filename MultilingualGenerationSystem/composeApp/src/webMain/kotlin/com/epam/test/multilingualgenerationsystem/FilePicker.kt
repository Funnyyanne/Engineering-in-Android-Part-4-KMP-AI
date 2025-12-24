package com.epam.test.multilingualgenerationsystem

import kotlinx.browser.document
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.get
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLInputElement
import org.w3c.files.FileReader
import org.w3c.files.get

actual fun openFilePicker(onFileSelected: (String, ByteArray?) -> Unit) {
    val input = document.createElement("input") as HTMLInputElement
    input.type = "file"
    input.style.display = "none"
    input.onchange = {
        val file = input.files?.get(0)
        if (file != null) {
            val reader = FileReader()
            reader.onload = {
                val arrayBuffer = reader.result as ArrayBuffer
                val int8Array = Int8Array(arrayBuffer)
                val byteArray = ByteArray(int8Array.length)
                for (i in 0 until int8Array.length) {
                    byteArray[i] = int8Array[i]
                }
                onFileSelected(file.name, byteArray)
            }
            reader.readAsArrayBuffer(file)
        }
    }
    document.body?.appendChild(input)
    input.click()
    document.body?.removeChild(input)
}

actual fun downloadFile(url: String, fileName: String) {
    val anchor = document.createElement("a") as HTMLAnchorElement
    anchor.href = url
    anchor.download = fileName
    anchor.click()
}
