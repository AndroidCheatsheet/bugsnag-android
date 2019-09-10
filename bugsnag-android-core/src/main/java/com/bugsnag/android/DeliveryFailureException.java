package com.bugsnag.android;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * This should be thrown if delivery of a request was not successful and you wish to try again
 * later. The notifier will cache the payload and initiate delivery at a future time.
 *
 * @see Delivery
 */
public class DeliveryFailureException extends Exception {
    private static final long serialVersionUID = 1501477209400426470L;

    public DeliveryFailureException(@NonNull String message) {
        super(message);
    }

    public DeliveryFailureException(@NonNull String message, @Nullable Throwable cause) {
        super(message, cause);
    }
}
