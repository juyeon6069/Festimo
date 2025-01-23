package com.example.festimo.domain.post.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

@Controller
@Slf4j
public class PlaceholderController {
    @GetMapping("/api/placeholder/{width}/{height}")
    @ResponseBody
    public ResponseEntity<?> getPlaceholderImage(@PathVariable int width, @PathVariable int height) {
        try {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();

            graphics.setColor(new Color(200, 200, 200));
            graphics.fillRect(0, 0, width, height);

            graphics.setColor(Color.WHITE);
            String text = width + "x" + height;
            graphics.drawString(text, width/3, height/2);

            graphics.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(baos.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("플레이스홀더 이미지를 생성하는 중 오류가 발생했습니다.", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}