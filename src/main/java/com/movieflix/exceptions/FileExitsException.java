package com.movieflix.exceptions;

public class FileExitsException extends RuntimeException {
    public FileExitsException(String message) {
        super(message);
    }
}
