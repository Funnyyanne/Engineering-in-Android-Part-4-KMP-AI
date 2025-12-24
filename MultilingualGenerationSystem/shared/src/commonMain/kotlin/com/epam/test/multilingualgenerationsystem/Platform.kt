package com.epam.test.multilingualgenerationsystem

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform