package com.example.dblog.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PostPageController {

    @GetMapping("/p/{hash}")
    public String postPage(@PathVariable String hash) {
        return "forward:/post.html";
    }
}
