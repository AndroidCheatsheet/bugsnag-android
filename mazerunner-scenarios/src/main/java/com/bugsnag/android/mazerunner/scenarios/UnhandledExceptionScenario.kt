package com.bugsnag.android.mazerunner.scenarios

import android.content.Context

import com.bugsnag.android.Configuration

/**
 * Sends an unhandled exception to Bugsnag.
 */
internal class UnhandledExceptionScenario(config: Configuration,
                                          context: Context) : Scenario(config, context) {
    init {
        config.autoCaptureSessions = false
    }

    override fun run() {
        super.run()
        throw generateException()
    }

}
