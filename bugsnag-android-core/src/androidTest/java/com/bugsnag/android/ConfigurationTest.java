package com.bugsnag.android;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collection;
import java.util.Map;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class ConfigurationTest {

    private Configuration config;

    @Before
    public void setUp() throws Exception {
        config = BugsnagTestUtils.generateConfiguration();
    }

    @Test
    public void testEndpoints() {
        String notify = "https://notify.myexample.com";
        String sessions = "https://sessions.myexample.com";
        config.setEndpoints(new Endpoints(notify, sessions));

        assertEquals(notify, config.getEndpoints().getNotify());
        assertEquals(sessions, config.getEndpoints().getSessions());
    }

    @Test
    public void testShouldNotify() {
        // Should notify if notifyReleaseStages is null
        ImmutableConfig immutableConfig;
        immutableConfig = createConfigWithReleaseStages(config,
                config.getNotifyReleaseStages(), "development");
        assertTrue(immutableConfig.shouldNotifyForReleaseStage());

        // Shouldn't notify if notifyReleaseStages is set and releaseStage is null
        immutableConfig = createConfigWithReleaseStages(config, new String[]{"example"}, null);
        assertFalse(immutableConfig.shouldNotifyForReleaseStage());

        // Shouldn't notify if releaseStage not in notifyReleaseStages
        String[] stages = {"production"};
        immutableConfig = createConfigWithReleaseStages(config, stages, "not-production");
        assertFalse(immutableConfig.shouldNotifyForReleaseStage());

        // Should notify if releaseStage in notifyReleaseStages
        immutableConfig = createConfigWithReleaseStages(config, stages, "production");
        assertTrue(immutableConfig.shouldNotifyForReleaseStage());
    }

    private ImmutableConfig createConfigWithReleaseStages(Configuration config,
                                                          String[] releaseStages,
                                                          String releaseStage) {
        config.setNotifyReleaseStages(releaseStages);
        config.setReleaseStage(releaseStage);
        return BugsnagTestUtils.convert(config);
    }

    @Test
    public void testLaunchThreshold() throws Exception {
        assertEquals(5000L, config.getLaunchCrashThresholdMs());

        config.setLaunchCrashThresholdMs(-5);
        assertEquals(0, config.getLaunchCrashThresholdMs());

        int expected = 1500;
        config.setLaunchCrashThresholdMs(expected);
        assertEquals(expected, config.getLaunchCrashThresholdMs());
    }

    @Test
    public void testAutoCaptureSessions() throws Exception {
        assertTrue(config.getAutoCaptureSessions());
        config.setAutoCaptureSessions(false);
        assertFalse(config.getAutoCaptureSessions());
    }

    @Test
    public void testOverrideContext() throws Exception {
        config.setContext("LevelOne");
        assertEquals("LevelOne", config.getContext());
    }

    @Test
    public void testOverrideFilters() throws Exception {
        config.setFilters(new String[]{"Foo"});
        assertArrayEquals(new String[]{"Foo"}, config.getFilters());
    }

    @Test
    public void testOverrideIgnoreClasses() throws Exception {
        config.setIgnoreClasses(new String[]{"Bar"});
        assertArrayEquals(new String[]{"Bar"}, config.getIgnoreClasses());
    }

    @Test
    public void testOverrideNotifyReleaseStages() throws Exception {
        config.setNotifyReleaseStages(new String[]{"Test"});
        assertArrayEquals(new String[]{"Test"}, config.getNotifyReleaseStages());
    }

    @Test
    public void testOverrideNotifierType() throws Exception {
        config.setNotifierType("React Native");
        assertEquals("React Native", config.getNotifierType());
    }

    @Test
    public void testOverrideCodeBundleId() throws Exception {
        config.setCodeBundleId("abc123");
        assertEquals("abc123", config.getCodeBundleId());
    }

    @Test
    public void testSetDelivery() {
        Configuration configuration = new Configuration("api-key");
        assertNull(configuration.getDelivery());
        Delivery delivery = BugsnagTestUtils.generateDelivery();
        configuration.setDelivery(delivery);

        assertFalse(configuration.getDelivery() instanceof DefaultDelivery);
        assertEquals(delivery, configuration.getDelivery());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetNullDelivery() {
        config.setDelivery(null);
    }

}
