package com.bugsnag.android;

import androidx.annotation.NonNull;

/**
 * A callback to be run before an individual report is sent to Bugsnag.
 * <p>
 * Use this to add or modify error report information before it is sent to
 * Bugsnag.
 *
 * @see com.bugsnag.android.Bugsnag#notify(Throwable, Callback)
 */
public interface Callback {

    void beforeNotify(@NonNull Report report);
}
