// src/main/java/com/sim/board/exception/SecurityException.java
package com.sim.board.exception;

public class SecurityException extends RuntimeException {

    public SecurityException(String message) {
        super(message);
    }

    public SecurityException(String message, Throwable cause) {
        super(message, cause);
    }
}