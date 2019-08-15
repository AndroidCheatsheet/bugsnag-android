package com.bugsnag.android.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import com.bugsnag.android.*
import com.example.foo.CrashyClass
import java.util.*

class ExampleActivity : AppCompatActivity() {

    companion object {
        init {
            System.loadLibrary("entrypoint")
        }
    }

    external fun doCrash()

    external fun notifyFromCXX()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        setupToolbarLogo()
        performAdditionalBugsnagSetup()

        val view: View = findViewById(R.id.btn_fatal_crash)
        view.setOnClickListener(::crashUnhandled)

        val nativeBtn: View = findViewById(R.id.btn_native_crash)
        nativeBtn.setOnClickListener { doCrash() }

        findViewById<View>(R.id.btn_anr).setOnClickListener { Thread.sleep(10000) }
    }

    private fun performAdditionalBugsnagSetup() {
        // Set the global user information
        Bugsnag.setUser("123456", "joebloggs@example.com", "Joe Bloggs")

        // Add some global metaData
        Bugsnag.addToTab("user", "age", 31)
    }

    /**
     * Throws an unhandled Exception. Bugsnag will automatically capture any uncaught exceptions
     * in your app and send an error report.
     */
    @Suppress("UNUSED_PARAMETER")
    fun crashUnhandled(view: View) {
        throw CrashyClass.crash("Fatal Crash")
    }

    /**
     * You can call [Bugsnag.notify] to send an error report for exceptions
     * which are already handled by your app.
     */
    @Suppress("UNUSED_PARAMETER")
    fun crashHandled(view: View) {
        try {
            throw RuntimeException("Non-Fatal Crash")
        } catch (e: RuntimeException) {
            Bugsnag.notify(e)
        }

        displayToastNotification()
    }

    /**
     * Delivers an error notification from native (C/C++) code
     */
    @Suppress("UNUSED_PARAMETER")
    fun notifyNativeHandled(view: View) {
        notifyFromCXX()
    }

    /**
     * The severity of error reports can be altered. This can be useful for capturing handled
     * exceptions which occur often but are not visible to the user.
     */
    @Suppress("UNUSED_PARAMETER")
    fun crashWithCustomSeverity(view: View) {
        val e = RuntimeException("Error Report with altered Severity")
        Bugsnag.notify(e, Severity.INFO)
        displayToastNotification()
    }

    /**
     * User details can be added globally, which will then appear in all error reports sent
     * to the Bugsnag dashboard.
     */
    @Suppress("UNUSED_PARAMETER")
    fun crashWithUserDetails(view: View) {
        Bugsnag.setUser("123456", "joebloggs@example.com", "Joe Bloggs")
        val e = RuntimeException("Error Report with User Info")
        Bugsnag.notify(e)
        displayToastNotification()
    }

    /**
     * Additional metadata can be attached to crash reports. This can be achieved by calling
     * [Bugsnag.notify], as shown below, or registering a global callback
     * with [Bugsnag.beforeNotify] that adds metadata to the report.
     */
    @Suppress("UNUSED_PARAMETER")
    fun crashWithMetadata(view: View) {
        val e = RuntimeException("Error report with Additional Metadata")

        Bugsnag.notify(e) {report ->
            val error = report.error
            error.severity = Severity.ERROR
            error.metaData.addToTab("CustomMetaData", "HasLaunchedGameTutorial", true)
        }
        displayToastNotification()
    }

    /**
     * Breadcrumbs help track down the cause of an error, by displaying events that happened leading
     * up to a crash. You can log your own breadcrumbs which will display on the Bugsnag Dashboard -
     * activity lifecycle callbacks and system intents are also captured automatically.
     */
    @Suppress("UNUSED_PARAMETER")
    fun crashWithBreadcrumbs(view: View) {
        Bugsnag.leaveBreadcrumb("LoginButtonClick")

        val metadata = HashMap<String, String>()
        metadata.put("reason", "Incorrect password")
        Bugsnag.leaveBreadcrumb("WebAuthFailure", BreadcrumbType.ERROR, metadata)

        val e = RuntimeException("Error Report with Breadcrumbs")
        Bugsnag.notify(e)
        displayToastNotification()
    }

    /**
     * When sending a handled error, a callback can be registered, which allows the Error Report
     * to be modified before it is sent.
     */
    @Suppress("UNUSED_PARAMETER")
    fun crashWithCallback(view: View) {
        val e = RuntimeException("Customized Error Report")

        Bugsnag.notify(e) { report ->
            // modify the report
            report.error.metaData = generateUserMetaData()
        }
        displayToastNotification()
    }

    private fun displayToastNotification() {
        Toast.makeText(this, "Error Report Sent!", LENGTH_SHORT).show()
    }

    private fun setupToolbarLogo() {
        val supportActionBar = supportActionBar

        if (supportActionBar != null) {
            supportActionBar.setDisplayShowHomeEnabled(true)
            supportActionBar.setIcon(R.drawable.ic_bugsnag_svg)
            supportActionBar.title = null
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun readDocs(view: View) {
        val uri = Uri.parse("https://docs.bugsnag.com/platforms/android/sdk/")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }

    private fun generateUserMetaData(): MetaData {
        val completedLevels = Arrays.asList("Level 1 - The Beginning", "Level 2 - Tower Defence")
        val userDetails = HashMap<String, String>()
        userDetails["playerName"] = "Joe Bloggs the Invincible"

        val metaData = MetaData()
        metaData.addToTab("CustomMetaData", "HasLaunchedGameTutorial", true)
        metaData.addToTab("CustomMetaData", "UserDetails", userDetails)
        metaData.addToTab("CustomMetaData", "CompletedLevels", completedLevels)
        return metaData
    }
}
