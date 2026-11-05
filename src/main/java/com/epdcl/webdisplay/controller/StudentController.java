package com.epdcl.webdisplay.controller;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class StudentController {

    @PostMapping("/student")
    public Map<String, String> getStudent(
            @RequestParam String name,
            @RequestParam String roll,
            @RequestParam String age,
            @RequestParam String department) {

        return Map.of(
                "name", name,
                "roll", roll,
                "age", age,
                "department", department
        );
    }
}