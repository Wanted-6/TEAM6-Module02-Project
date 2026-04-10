package com.wanted.projectmodule2lms.global.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping(value = {"/", "/main"})
    public String mainPage() {
        return "main/main";
    }
}