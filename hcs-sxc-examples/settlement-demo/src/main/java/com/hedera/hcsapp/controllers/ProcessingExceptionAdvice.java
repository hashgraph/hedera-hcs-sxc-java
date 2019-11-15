package com.hedera.hcsapp.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
class ProcessingExceptionAdvice {

  @ResponseBody
  @ExceptionHandler(ProcessingException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  String processingExceptionHandler(ProcessingException ex) {
    return ex.getMessage();
  }
}