package com.epam.test.multilingualgenerationsystem

expect fun openFilePicker(onFileSelected: (String, ByteArray?) -> Unit)

expect fun downloadFile(url: String, fileName: String)
