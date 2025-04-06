package com.sena.barberspa.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(OAuth2AuthenticationException.class)
    public ModelAndView handleOAuth2AuthenticationException(OAuth2AuthenticationException ex, HttpServletRequest request) {
        OAuth2Error error = ex.getError();
        logger.error("OAuth2 authentication error: {} - {}", error.getErrorCode(), error.getDescription());

        ModelAndView mav = new ModelAndView();
        mav.addObject("errorCode", error.getErrorCode());
        mav.addObject("errorDescription", error.getDescription());
        mav.addObject("requestUri", request.getRequestURI());
        mav.setViewName("error/oauth2_error");
        return mav;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleGenericException(Exception ex) {
        logger.error("Unexpected error during OAuth flow: {}", ex.getMessage(), ex);

        ModelAndView mav = new ModelAndView();
        mav.addObject("errorMessage", "Error inesperado: " + ex.getMessage());
        mav.setViewName("error/error");
        return mav;
    }
}