package com.sena.barberspa.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ErrorHandlerController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        Exception exception = (Exception) request.getAttribute("javax.servlet.error.exception");

        model.addAttribute("statusCode", statusCode);

        if (exception != null) {
            model.addAttribute("errorMessage", exception.getMessage());
        }

        return "error/error";
    }

    @GetMapping("/error/oauth2_error")
    public String handleOAuth2Error() {
        return "error/oauth2_error";
    }

    @GetMapping("/error/403")
    public String handleAccessDenied() {
        return "error/403";
    }
}