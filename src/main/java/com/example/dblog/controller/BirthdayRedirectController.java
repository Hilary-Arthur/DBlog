package com.example.dblog.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BirthdayRedirectController {

    @GetMapping("/birthday")
    public String redirectBirthday() {
        return "redirect:http://localhost:8080/";
    }

    @GetMapping("/birthday/**")
    public String redirectBirthdaySub() {
        return "redirect:http://localhost:8080/";
    }
}
