package com.bugsnag.android.mazerunner.scenarios

import android.content.Context
import com.bugsnag.android.Bugsnag
import com.bugsnag.android.Configuration
import android.os.Handler
import android.os.HandlerThread
import com.bugsnag.android.flushAllSessions

/**
 * Sends an exception after stopping the session
 */
internal class NewSessionScenario(config: Configuration,
                                      context: Context) : Scenario(config, context) {

    override fun run() {
        super.run()
        val client = Bugsnag.getClient()
        val thread = HandlerThread("HandlerThread")
        thread.start()

        Handler(thread.looper).post {
            // send 1st exception which should include session info
            client.startSession()
            Thread.sleep(100)
            flushAllSessions()
            Thread.sleep(100)
            client.notifyBlocking(generateException())
            Thread.sleep(100)
            // stop tracking the existing session
            client.stopSession()
            Thread.sleep(100)
            // send 2nd exception which should contain new session info
            client.startSession()
            Thread.sleep(100)
            flushAllSessions()
            Thread.sleep(100)
            client.notifyBlocking(generateException())
        }
    }
}
