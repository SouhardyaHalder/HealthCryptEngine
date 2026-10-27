package org.example.healthcryptengine.controller;

import org.example.healthcryptengine.model.User;
import org.example.healthcryptengine.repository.Repository;
import org.example.healthcryptengine.service.GoogleCalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.api.client.auth.oauth2.Credential;
import jakarta.servlet.http.HttpSession;

@Controller
public class GoogleAuthController {

    @Autowired
    private GoogleCalendarService calendarService;

    @Autowired
    private Repository userRepository;
    
    // Redirect user to Google login
    @GetMapping("/google/auth")
    public String googleAuth() throws Exception {
        return "redirect:" + calendarService.getAuthUrl();
    }

    // Google calls this after user logs in
    @GetMapping("/oauth2/callback")
    public String oauth2Callback(@RequestParam String code,
                                  HttpSession session) throws Exception {
        Credential credential = calendarService.getCredential(code);
        
        session.setAttribute("googleCredential", credential);

        // Save token to DB against logged in user
        User user = (User) session.getAttribute("loggedInUser");
        if (user != null) {
            user.setGoogleAccessToken(credential.getAccessToken());
            user.setGoogleRefreshToken(credential.getRefreshToken());
            user.setGoogleTokenExpiry(credential.getExpirationTimeMilliseconds());
            userRepository.save(user);
        }
        
        return "redirect:/pomodoro";
    }
}