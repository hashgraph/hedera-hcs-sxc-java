package com.hedera.hcsapp.controllers;
class ProcessingException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    ProcessingException(Exception e) {
        super("Processing error occurred" + e.getMessage());
    }
}