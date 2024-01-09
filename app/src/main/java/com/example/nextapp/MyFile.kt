package com.example.nextapp

import java.io.File

class MyFile(private val path: String, private val name: String) {
    fun exists(): Boolean {
        return File("$path/$name").exists()
    }

    fun createNewFile(): File {
        return File("$path/$name").apply {
            createNewFile()
        }
    }
}
