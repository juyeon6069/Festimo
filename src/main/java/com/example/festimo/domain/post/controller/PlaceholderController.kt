package com.example.festimo.domain.post.controller

import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseBody
import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

@Controller
class PlaceholderController {
    private val logger = LoggerFactory.getLogger(javaClass)

    private inline fun <T> Graphics2D.withGraphics(block: (Graphics2D) -> T): T {
        try {
            return block(this)
        } finally {
            dispose()
        }
    }

    @GetMapping("/api/placeholder/{width}/{height}")
    @ResponseBody
    fun getPlaceholderImage(
        @PathVariable width: Int,
        @PathVariable height: Int
    ): ResponseEntity<ByteArray> = try {
        BufferedImage(width, height, BufferedImage.TYPE_INT_RGB).let { image ->
            image.createGraphics().withGraphics { graphics ->
                graphics.color = Color(200, 200, 200)
                graphics.fillRect(0, 0, width, height)
                graphics.color = Color.WHITE
                graphics.drawString("${width}x$height", width/3, height/2)

                ByteArrayOutputStream().use { baos ->
                    ImageIO.write(image, "png", baos)
                    ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_PNG)
                        .body(baos.toByteArray())
                }
            }
        }
    } catch (e: Exception) {
        logger.error("플레이스홀더 이미지를 생성하는 중 오류가 발생했습니다.", e)
        ResponseEntity.internalServerError().build()
    }
}