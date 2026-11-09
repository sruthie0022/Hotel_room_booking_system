package com.hotelbooking.exception;

import com.hotelbooking.controller.AdminController;
import com.hotelbooking.controller.AuthController;
import com.hotelbooking.controller.BookingController;
import com.hotelbooking.controller.SiteController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice(assignableTypes = {AdminController.class, AuthController.class,
        BookingController.class, SiteController.class})
public class MvcExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    ModelAndView handleNotFound() {
        return new ModelAndView("error/404", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BusinessException.class)
    ModelAndView handleBusinessRule() {
        return new ModelAndView("error/409", HttpStatus.CONFLICT);
    }
}