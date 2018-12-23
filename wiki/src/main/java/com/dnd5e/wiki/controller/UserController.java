package com.dnd5e.wiki.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.dnd5e.wiki.model.user.User;
import com.dnd5e.wiki.service.SecurityService;
import com.dnd5e.wiki.service.UserService;

@Controller
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private SecurityService securityService;

    @GetMapping("/registration")
    public String registration(Model model) {
        model.addAttribute("user", new User());
        return "/user/registration";
    }

    @PostMapping("/registration")
    public String registration(@Valid  @ModelAttribute("user") User userForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "/user/registration";
        }
        userService.save(userForm);

        securityService.autologin(userForm.getName(), userForm.getPasswordConfirm());

        return "redirect:/tavern?sort=name,asc";
    }

    @GetMapping("/login")
    public String login(Model model, String error, String logout) {
        if (error != null)
            model.addAttribute("error", "Ваше имя пользователя или пароль неверны.");

        if (logout != null)
            model.addAttribute("message", "You have been logged out successfully.");

        return "/user/login";
    }
}