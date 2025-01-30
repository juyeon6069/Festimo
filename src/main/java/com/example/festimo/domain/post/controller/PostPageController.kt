package com.example.festimo.domain.post.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class PostPageController {
    @GetMapping(
        value = [
            "/",
            "/community/**",
            "/login",
            "/register",
            "/post/**",
            "/post/edit/**",
            "/post/write"
        ]
    )
    fun forward(): String = "forward:/index.html"
}