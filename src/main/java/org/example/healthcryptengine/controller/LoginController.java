package org.example.healthcryptengine.controller;

import jakarta.servlet.http.HttpSession;
import org.example.healthcryptengine.model.User;
import org.example.healthcryptengine.repository.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;

@Controller
public class LoginController {
    @Autowired
    private Repository userRepository;

    @GetMapping("/login")
    public String showLoginPage() {
        return "login"; // maps to login.html
    }

    @PostMapping("/login")
    public String handleLogin(@RequestParam String username , @RequestParam String password, Model model, HttpSession session){
        User user = userRepository.findByUsername(username);

        if(user == null){
            model.addAttribute("error", "Username not found");
            return "login";
        }
        if(!user.getPassword().equals(password)){
            model.addAttribute("error", "Incorrect Password");
            return "login";
        }

        session.setAttribute("loggedInUser",user);
        return "redirect:/home";
    }


    @GetMapping("/signup")
    public String showSignupPage() {
        return "signup"; // will look for signup.html in templates
    }

    @PostMapping("/signup")
    public String handleSignup(@RequestParam String username,
                               @RequestParam String password,
                               Model model) {

        if (userRepository.findByUsername(username) != null) {
            model.addAttribute("error", "Username already exists!");
            return "signup";
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        userRepository.save(user);

        return "redirect:/login"; // ✅ account created, go login
    }

    // ---------- HOME ----------
    @GetMapping("/home")
    public String showHomePage() {
        return "home";
    }
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // ✅ clears all session data
        return "redirect:/login";
    }
}