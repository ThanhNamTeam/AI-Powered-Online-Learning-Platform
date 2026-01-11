package com.minhkhoi.swd392.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping
@RestController
public class AccountController {


    @GetMapping
    public void hello() {
        System.out.println("Hello, World!");
    }
}
