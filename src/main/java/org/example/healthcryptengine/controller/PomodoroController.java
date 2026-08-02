package org.example.healthcryptengine.controller;

import org.example.healthcryptengine.service.GoogleCalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

@Controller
public class PomodoroController {

    @Autowired
    private GoogleCalendarService calendarService;

    // ✅ Only ONE /pomodoro mapping
    @GetMapping("/pomodoro")
    public String showPomodoroPage(HttpSession session, Model model) {
        Object credential = session.getAttribute("googleCredential");
        if (credential != null) {
            model.addAttribute("googleConnected", true);
        }
        return "pomodoro";
    }

    @GetMapping("/pomodoro/log")
    public String logSession(@RequestParam int duration, HttpSession session) {
        try {
            var credential = session.getAttribute("googleCredential");
            if (credential != null) {
                calendarService.createPomodoroEvent(
                    (com.google.api.client.auth.oauth2.Credential) credential, duration);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/pomodoro";
    }
}