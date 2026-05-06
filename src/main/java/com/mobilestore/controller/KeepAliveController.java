package com.mobilestore.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class KeepAliveController {

    // Jab bhi koi /keep-alive url par aayega, yeh method chalega
    @GetMapping("/keep-alive")
    public String keepAlive() {
        return "Server is active and awake!";
    }
}