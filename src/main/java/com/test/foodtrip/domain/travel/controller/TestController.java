package com.test.foodtrip.domain.travel.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestController {

    @GetMapping("/trip/travel")
    public String travel() {

        return "test";
    }
}
