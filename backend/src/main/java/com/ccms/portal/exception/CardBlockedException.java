package com.ccms.portal.exception;

/**
 * Exception thrown when attempting to perform operations on a blocked credit card.
 * This includes creating transactions, making payments, or any other card operations
 * that require the card to be in an active state.
 */
public class CardBlockedException extends RuntimeException {
    
    public CardBlockedException(String message) {
        super(message);
    }
    
    public CardBlockedException(String message, Throwable cause) {
        super(message, cause);
    }
}
