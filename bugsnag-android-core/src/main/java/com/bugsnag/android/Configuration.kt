package com.bugsnag.android

import android.content.Context
import android.text.TextUtils
import java.util.Collections
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * User-specified configuration storage object, contains information
 * specified at the client level, api-key and endpoint configuration.
 */
class Configuration(
    /**
     * Gets the API key to send reports to
     */
    val apiKey: String
) : CallbackAware, MetadataAware {

    var metadata = Metadata()
    protected val onErrorTasks = ConcurrentLinkedQueue<OnError>()
    protected val breadcrumbCallbacks = ConcurrentLinkedQueue<OnBreadcrumb>()
    protected val sessionCallbacks = ConcurrentLinkedQueue<OnSession>()

    /**
     * Set the buildUUID to your own value. This is used to identify proguard
     * mapping files in the case that you publish multiple different apps with
     * the same appId and versionCode. The default value is read from the
     * com.bugsnag.android.BUILD_UUID meta-data field in your app manifest.
     */
    var buildUuid: String? = null

    /**
     * Set the application version sent to Bugsnag. By default we'll pull this
     * from your AndroidManifest.xml
     */
    var appVersion: String? = null

    /**
     * Set the version code sent to Bugsnag. By default we'll pull this
     * from your AndroidManifest.xml
     */
    var versionCode: Int? = 0

    /**
     * Set the current "release stage" of your application.
     * By default, we'll set this to "development" for debug builds and
     * "production" for non-debug builds.
     *
     * @see .setEnabledReleaseStages
     */
    var releaseStage: String? = null

    /**
     * Set whether to send thread-state with report.
     * By default, this will be true.
     */
    var sendThreads = true

    /**
     * Set whether or not Bugsnag should persist user information between application settings
     * if set then any user information set will be re-used until
     */
    var persistUserBetweenSessions = false

    /**
     * Sets the threshold in ms for an uncaught error to be considered as a crash on launch.
     * If a crash is detected on launch, Bugsnag will attempt to send the report synchronously.
     *
     * The app's launch time is tracked as the time at which [Bugsnag.init] was
     * called.
     *
     * By default, this value is set at 5,000ms.
     */
    var launchCrashThresholdMs: Long = DEFAULT_LAUNCH_CRASH_THRESHOLD_MS
        set(launchCrashThresholdMs) {
            field = when {
                launchCrashThresholdMs <= MIN_LAUNCH_CRASH_THRESHOLD_MS -> MIN_LAUNCH_CRASH_THRESHOLD_MS
                else -> launchCrashThresholdMs
            }
        }

    /**
     * Sets whether or not Bugsnag should automatically capture and report User sessions whenever
     * the app enters the foreground.
     *
     * By default this behavior is enabled.
     */
    var autoTrackSessions = true

    /**
     * Sets whether [ANRs](https://developer.android.com/topic/performance/vitals/anr)
     * should be reported to Bugsnag. When enabled, Bugsnag will record an ANR whenever the main
     * thread has been blocked for 5000 milliseconds or longer.
     *
     * If you wish to enable ANR detection, you should set this property to true.
     */
    var autoDetectAnrs = false

    /**
     * Determines whether NDK crashes such as signals and exceptions should be reported by bugsnag.
     *
     * If you are using bugsnag-android this flag is false by default; if you are using
     * bugsnag-android-ndk this flag is true by default.
     */
    var autoDetectNdkCrashes: Boolean = false

    /**
     * Sets whether Bugsnag should automatically capture and report unhandled errors.
     * By default, this value is true.
     */
    var autoDetectErrors = true

    /**
     * Intended for internal use only - sets the code bundle id for React Native
     */
    var codeBundleId: String? = null

    /**
     * Intended for internal use only - sets the type of the notifier (e.g. Android, React Native)
     */
    var appType = "android"

    /**
     * Sets the delivery used to make HTTP requests to Bugsnag. A default implementation is
     * provided, but you may wish to use your own implementation if you have requirements such
     * as pinning SSL certificates, for example.
     *
     * Any custom implementation must be capable of sending
     * [Error Reports](https://docs.bugsnag.com/api/error-reporting/)
     * and [Sessions](https://docs.bugsnag.com/api/sessions/) as
     * documented at [https://docs.bugsnag.com/api/](https://docs.bugsnag.com/api/)
     */
    var delivery: Delivery? = null

    /**
     * Set the endpoints to send data to. By default we'll send error reports to
     * https://notify.bugsnag.com, and sessions to https://sessions.bugsnag.com, but you can
     * override this if you are using Bugsnag Enterprise to point to your own Bugsnag endpoints.
     */
    var endpoints = Endpoints()

    /**
     * Set the maximum number of breadcrumbState to keep and sent to Bugsnag.
     * By default, we'll keep and send the 25 most recent breadcrumb log
     * messages.
     */
    var maxBreadcrumbs = DEFAULT_MAX_SIZE
        set(numBreadcrumbs) {
            field = when {
                numBreadcrumbs <= MIN_BREADCRUMBS -> MIN_BREADCRUMBS
                numBreadcrumbs > MAX_BREADCRUMBS -> MAX_BREADCRUMBS
                else -> numBreadcrumbs
            }
        }

    /**
     * Set the context sent to Bugsnag. By default we'll attempt to detect the
     * name of the top-most activity at the time of a report, and use this
     * as the context, but sometime this is not possible.
     */
    var context: String? = null

    /**
     * Set which keys should be redacted when sending metadata to Bugsnag.
     * Use this when you want to ensure sensitive information, such as passwords
     * or credit card information is stripped from metadata you send to Bugsnag.
     * Any keys in metadata which contain these strings will be marked as
     * REDACTED when send to Bugsnag.
     *
     * For example:
     *
     * client.setRedactKeys("password", "credit_card");
     */
    var redactKeys: Set<String>
        get() = Collections.unmodifiableSet(metadata.redactKeys)
        set(redactKeys) = metadata.setRedactKeys(redactKeys)


    var loggingEnabled: Boolean

    init {
        require(!TextUtils.isEmpty(apiKey)) { "You must provide a Bugsnag API key" }
        autoDetectNdkCrashes = try {
            // check if AUTO_DETECT_NDK_CRASHES has been set in bugsnag-android
            // or bugsnag-android-ndk
            val clz = Class.forName("com.bugsnag.android.BuildConfig")
            val field = clz.getDeclaredField("AUTO_DETECT_NDK_CRASHES")
            field.getBoolean(null)
        } catch (exc: Throwable) {
            false
        }

        loggingEnabled = AppData.RELEASE_STAGE_PRODUCTION != releaseStage
    }

    /**
     * Set which exception classes should be ignored (not sent) by Bugsnag.
     *
     * For example:
     *
     * client.setIgnoreClasses("java.lang.RuntimeException");
     */
    var ignoreClasses: Set<String> = emptySet()

    /**
     * Set for which releaseStages errors should be sent to Bugsnag.
     * Use this to stop errors from development builds being sent.
     *
     * For example:
     *
     * client.setEnabledReleaseStages("production");
     */
    var enabledReleaseStages: Set<String> = emptySet()

    var enabledBreadcrumbTypes: Set<BreadcrumbType> = BreadcrumbType.values().toSet()

    /**
     * Set which packages should be considered part of your application.
     * Bugsnag uses this to help with error grouping, and stacktrace display.
     *
     * For example:
     *
     * client.setProjectPackages("com.example.myapp");
     *
     * By default, we'll mark the current package name as part of you app.
     */
    var projectPackages: Set<String> = emptySet()

    /**
     * Add a "on error" callback, to execute code at the point where an error report is
     * captured in Bugsnag.
     *
     * You can use this to add or modify information attached to an Event
     * before it is sent to your dashboard. You can also return
     * `false` from any callback to prevent delivery. "on error"
     * callbacks do not run before reports generated in the event
     * of immediate app termination from crashes in C/C++ code.
     *
     * For example:
     *
     * Bugsnag.addOnError(new OnError() {
     * public boolean run(Event event) {
     * event.setSeverity(Severity.INFO);
     * return true;
     * }
     * })
     *
     * @param onError a callback to run before sending errors to Bugsnag
     * @see OnError
     */
    override fun addOnError(onError: OnError) {
        if (!onErrorTasks.contains(onError)) {
            onErrorTasks.add(onError)
        }
    }

    override fun removeOnError(onError: OnError) {
        onErrorTasks.remove(onError)
    }

    /**
     * Adds an on breadcrumb callback
     *
     * @param onBreadcrumb the on breadcrumb callback
     */
    override fun addOnBreadcrumb(onBreadcrumb: OnBreadcrumb) {
        if (!breadcrumbCallbacks.contains(onBreadcrumb)) {
            breadcrumbCallbacks.add(onBreadcrumb)
        }
    }

    /**
     * Removes an on breadcrumb callback
     *
     * @param onBreadcrumb the on breadcrumb callback
     */
    override fun removeOnBreadcrumb(onBreadcrumb: OnBreadcrumb) {
        breadcrumbCallbacks.remove(onBreadcrumb)
    }

    /**
     * Adds an on session callback
     *
     * @param onSession the on session callback
     */
    override fun addOnSession(onSession: OnSession) {
        if (!sessionCallbacks.contains(onSession)) {
            sessionCallbacks.add(onSession)
        }
    }

    /**
     * Removes an on session callback
     *
     * @param onSession the on session callback
     */
    override fun removeOnSession(onSession: OnSession) {
        sessionCallbacks.remove(onSession)
    }

    override fun addMetadata(section: String, value: Any?) =
        metadata.addMetadata(section, value)
    override fun addMetadata(section: String, key: String?, value: Any?) =
        metadata.addMetadata(section, key, value)

    override fun clearMetadata(section: String) = metadata.clearMetadata(section)
    override fun clearMetadata(section: String, key: String?) =
        metadata.clearMetadata(section, key)

    override fun getMetadata(section: String) = metadata.getMetadata(section)
    override fun getMetadata(section: String, key: String?) =metadata.getMetadata(section, key)

    companion object {
        private const val DEFAULT_MAX_SIZE = 25
        private const val DEFAULT_LAUNCH_CRASH_THRESHOLD_MS: Long = 5000
        private const val MIN_BREADCRUMBS = 0
        private const val MAX_BREADCRUMBS = 100
        private const val MIN_LAUNCH_CRASH_THRESHOLD_MS: Long = 0

        /**
         * Constructs a new Bugsnag Configuration object by looking for meta-data elements in
         * the AndroidManifest.xml
         *
         * @return a new Configuration object
         */
        fun loadConfig(ctx: Context): Configuration {
            return ManifestConfigLoader().load(ctx)
        }
    }
}
