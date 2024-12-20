package com.online.banking.Back_End_Banking_System.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
public class GreetingController {

    @Autowired
    private MessageSource messageSource;

    @GetMapping("/greet")
    public String greet(@RequestParam(name = "lang", required = false) String lang) {
        Locale locale = lang != null ? Locale.forLanguageTag(lang) : Locale.ENGLISH;
        return messageSource.getMessage("greeting", null, locale);
    }
}
