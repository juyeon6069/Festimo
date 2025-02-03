package com.example.festimo.domain.post.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.*

@Service
class FileService {
    @Value("\${file.upload-dir}")
    private lateinit var uploadDir: String

    fun saveFile(file: MultipartFile): String {
        val fileName = "${UUID.randomUUID()}_${file.originalFilename}"
        val path = Paths.get(uploadDir, fileName)
        Files.createDirectories(path.parent)
        Files.copy(file.inputStream, path, StandardCopyOption.REPLACE_EXISTING)
        return fileName
    }

    fun deleteFile(filePath: String) {
        val path = Paths.get(uploadDir, filePath)
        Files.deleteIfExists(path)
    }
}