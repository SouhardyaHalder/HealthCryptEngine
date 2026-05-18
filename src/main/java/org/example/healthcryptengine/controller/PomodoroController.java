package org.example.healthcryptengine.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PomodoroController {

    @GetMapping("/pomodoro")
    public String showPomodoroPage() {
        return "pomodoro";
    }
}