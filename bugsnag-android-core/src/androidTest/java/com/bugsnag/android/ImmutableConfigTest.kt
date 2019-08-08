package com.bugsnag.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ImmutableConfigTest {

    private val seed = Configuration("api-key")

    @Mock
    lateinit var delivery: Delivery

    @Before
    fun setUp() {
        // these options are required, but are set in the Client constructor if no value is set
        // on the config object
        seed.delivery = delivery
    }

    @Test
    fun defaultConversion() {
        with(convertToImmutableConfig(seed)) {
            assertEquals("api-key", apiKey)

            // detection
            assertTrue(autoCaptureBreadcrumbs)
            assertTrue(autoCaptureSessions)
            assertTrue(autoNotify)
            assertFalse(detectAnrs)
            assertFalse(detectNdkCrashes)
            assertTrue(sendThreads)

            // release stages
            assertTrue(ignoreClasses.isEmpty())
            assertTrue(notifyReleaseStages.isEmpty())
            assertTrue(projectPackages.isEmpty())
            assertEquals(seed.releaseStage, releaseStage)

            // identifiers
            assertEquals(seed.appVersion, appVersion)
            assertEquals(seed.buildUuid, buildUuid)
            assertEquals(seed.codeBundleId, codeBundleId)
            assertEquals(seed.notifierType, notifierType)

            // network config
            assertEquals(seed.delivery, delivery)
            assertEquals(seed.endpoints, endpoints)

            // behaviour
            assertEquals(seed.launchCrashThresholdMs, launchCrashThresholdMs)
            assertEquals(seed.loggingEnabled, loggingEnabled)
            assertEquals(seed.maxBreadcrumbs, maxBreadcrumbs)
            assertEquals(seed.persistUserBetweenSessions, persistUserBetweenSessions)
        }
    }


    @Test
    fun convertWithOverrides() {
        seed.autoCaptureBreadcrumbs = false
        seed.autoCaptureSessions = false
        seed.autoNotify = false
        seed.detectAnrs = true
        seed.detectNdkCrashes = true
        seed.sendThreads = false

        seed.ignoreClasses = arrayOf("foo")
        seed.notifyReleaseStages = arrayOf("bar")
        seed.projectPackages = arrayOf("com.example")
        seed.releaseStage = "wham"

        seed.appVersion = "1.2.3"
        seed.buildUuid = "f7ab"
        seed.codeBundleId = "codebundle123"
        seed.notifierType = "custom"

        seed.endpoints = Endpoints("http://example.com:1234", "http://example.com:1235")
        seed.launchCrashThresholdMs = 7000
        seed.loggingEnabled = false
        seed.maxBreadcrumbs = 37
        seed.persistUserBetweenSessions = true

        // verify overrides are copied across
        with(convertToImmutableConfig(seed)) {
            assertEquals("api-key", apiKey)

            // detection
            assertFalse(autoCaptureBreadcrumbs)
            assertFalse(autoCaptureSessions)
            assertFalse(autoNotify)
            assertTrue(detectAnrs)
            assertTrue(detectNdkCrashes)
            assertFalse(sendThreads)

            // release stages
            assertEquals(listOf("foo"), ignoreClasses)
            assertEquals(listOf("bar"), notifyReleaseStages)
            assertEquals(listOf("com.example"), projectPackages)
            assertEquals("wham", releaseStage)

            // identifiers
            assertEquals("1.2.3", seed.appVersion)
            assertEquals("f7ab", seed.buildUuid)
            assertEquals("codebundle123", seed.codeBundleId)
            assertEquals("custom", seed.notifierType)

            // network config
            assertEquals(
                seed.endpoints,
                Endpoints("http://example.com:1234", "http://example.com:1235")
            )

            // behaviour
            assertEquals(7000, seed.launchCrashThresholdMs)
            assertFalse(seed.loggingEnabled)
            assertEquals(37, seed.maxBreadcrumbs)
            assertTrue(seed.persistUserBetweenSessions)
        }
    }

    @Test
    fun verifyErrorApiHeaders() {
        val config = convertToImmutableConfig(seed)
        val headers = config.errorApiDeliveryParams().headers
        assertEquals(config.apiKey, headers["Bugsnag-Api-Key"])
        assertNotNull(headers["Bugsnag-Sent-At"])
        assertNotNull(headers["Bugsnag-Payload-Version"])
    }

    @Test
    fun verifySessionApiHeaders() {
        val config = convertToImmutableConfig(seed)
        val headers = config.sessionApiDeliveryParams().headers
        assertEquals(config.apiKey, headers["Bugsnag-Api-Key"])
        assertNotNull(headers["Bugsnag-Sent-At"])
        assertNotNull(headers["Bugsnag-Payload-Version"])
    }
}
