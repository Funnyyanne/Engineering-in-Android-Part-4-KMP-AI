package com.epam.test.multilingualgenerationsystem

import platform.UIKit.*
import platform.Foundation.*
import platform.darwin.NSObject
import kotlinx.cinterop.*

private var pickerDelegate: UIDocumentPickerDelegateProtocol? = null

@OptIn(ExperimentalForeignApi::class)
actual fun openFilePicker(onFileSelected: (String, ByteArray?) -> Unit) {
    println("openFilePicker called")
    /*
    val documentPicker = UIDocumentPickerViewController(
        forOpeningContentTypes = listOf(UTTypeData),
        asCopy = true
    )
    
    val delegate = object : NSObject(), UIDocumentPickerDelegateProtocol {
        override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
            val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL ?: return
            val data = NSData.dataWithContentsOfURL(url)
            val bytes = data?.let { nsData ->
                val size = nsData.length.toInt()
                val byteArray = ByteArray(size)
                if (size > 0) {
                    byteArray.usePinned { pinned ->
                        platform.posix.memcpy(pinned.addressOf(0), nsData.bytes, nsData.length)
                    }
                }
                byteArray
            }
            onFileSelected(url.lastPathComponent ?: "unknown", bytes)
            pickerDelegate = null
        }

        override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
            pickerDelegate = null
        }
    }
    
    pickerDelegate = delegate
    documentPicker.delegate = delegate
    
    val root = UIApplication.sharedApplication.keyWindow?.rootViewController
    root?.presentViewController(documentPicker, animated = true, completion = null)
    */
}

actual fun downloadFile(url: String, fileName: String) {
    println("downloadFile called")
    /*
    val nsUrl = NSURL.URLWithString(url) ?: return
    val activityViewController = UIActivityViewController(listOf(nsUrl), null)
    
    val root = UIApplication.sharedApplication.keyWindow?.rootViewController
    root?.presentViewController(activityViewController, animated = true, completion = null)
    */
}