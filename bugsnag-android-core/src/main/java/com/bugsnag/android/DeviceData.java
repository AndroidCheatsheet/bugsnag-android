package com.bugsnag.android;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.BatteryManager;
import android.os.Environment;
import android.provider.Settings;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

class DeviceData {

    private static final String[] ROOT_INDICATORS = new String[]{
        // Common binaries
        "/system/xbin/su",
        "/system/bin/su",
        // < Android 5.0
        "/system/app/Superuser.apk",
        "/system/app/SuperSU.apk",
        // >= Android 5.0
        "/system/app/Superuser",
        "/system/app/SuperSU",
        // Fallback
        "/system/xbin/daemonsu",
        // Systemless root
        "/su/bin"
    };

    private final boolean emulator;
    private final Context appContext;
    private final Connectivity connectivity;
    private final Resources resources;
    private final String installId;
    private final DeviceBuildInfo buildInfo;
    private final DisplayMetrics displayMetrics;
    private final boolean rooted;

    @Nullable
    final Float screenDensity;

    @Nullable
    final Integer dpi;

    @Nullable
    final String screenResolution;

    @NonNull
    final String locale;

    @NonNull
    final String[] cpuAbi;
    private final File dataDirectory;

    private final Logger logger;

    DeviceData(Connectivity connectivity, Context appContext, Resources resources,
               String installId, DeviceBuildInfo buildInfo, File dataDirectory, Logger logger) {
        this.connectivity = connectivity;
        this.appContext = appContext;
        this.resources = resources;
        this.installId = installId;
        this.buildInfo = buildInfo;
        this.logger = logger;

        if (resources != null) {
            displayMetrics = resources.getDisplayMetrics();
        } else {
            displayMetrics = null;
        }

        screenDensity = getScreenDensity();
        dpi = getScreenDensityDpi();
        screenResolution = getScreenResolution();
        locale = Locale.getDefault().toString();
        cpuAbi = getCpuAbi();
        emulator = isEmulator();
        rooted = isRooted();
        this.dataDirectory = dataDirectory;
    }

    Map<String, Object> getDeviceDataSummary() {
        Map<String, Object> map = new HashMap<>();
        map.put("manufacturer", buildInfo.getManufacturer());
        map.put("model", buildInfo.getModel());
        map.put("jailbroken", rooted);
        map.put("osName", "android");
        map.put("osVersion", buildInfo.getOsVersion());
        map.put("cpuAbi", cpuAbi);

        Map<String, Object> versions = new HashMap<>();
        versions.put("androidApiLevel", buildInfo.getApiLevel());
        versions.put("osBuild", buildInfo.getOsBuild());
        map.put("runtimeVersions", versions);
        return map;
    }

    Map<String, Object> getDeviceData() {
        Map<String, Object> map = getDeviceDataSummary();
        map.put("id", installId);
        map.put("freeMemory", calculateFreeMemory());
        map.put("totalMemory", calculateTotalMemory());
        map.put("freeDisk", calculateFreeDisk());
        map.put("orientation", calculateOrientation());
        map.put("time", getTime());
        map.put("locale", locale);
        return map;
    }

    Map<String, Object> getDeviceMetadata() {
        Map<String, Object> map = new HashMap<>();
        map.put("batteryLevel", getBatteryLevel());
        map.put("charging", isCharging());
        map.put("locationStatus", getLocationStatus());
        map.put("networkAccess", getNetworkAccess());
        map.put("brand", buildInfo.getBrand());
        map.put("screenDensity", screenDensity);
        map.put("dpi", dpi);
        map.put("emulator", emulator);
        map.put("screenResolution", screenResolution);
        return map;
    }

    /**
     * Check if the current Android device is rooted
     */
    private boolean isRooted() {
        if (buildInfo.getTags().contains("test-keys")) {
            return true;
        }

        try {
            for (String candidate : ROOT_INDICATORS) {
                if (new File(candidate).exists()) {
                    return true;
                }
            }
        } catch (Exception ignore) {
            return false;
        }
        return false;
    }

    /**
     * Guesses whether the current device is an emulator or not, erring on the side of caution
     *
     * @return true if the current device is an emulator
     */
    private boolean isEmulator() {
        String fingerprint = buildInfo.getFingerprint();
        return fingerprint.startsWith("unknown")
            || fingerprint.contains("generic")
            || fingerprint.contains("vbox"); // genymotion
    }

    /**
     * The screen density scaling factor of the current Android device
     */
    @Nullable
    private Float getScreenDensity() {
        if (displayMetrics != null) {
            return displayMetrics.density;
        } else {
            return null;
        }
    }

    /**
     * The screen density of the current Android device in dpi, eg. 320
     */
    @Nullable
    private Integer getScreenDensityDpi() {
        if (displayMetrics != null) {
            return displayMetrics.densityDpi;
        } else {
            return null;
        }
    }

    /**
     * The screen resolution of the current Android device in px, eg. 1920x1080
     */
    @Nullable
    private String getScreenResolution() {
        if (displayMetrics != null) {
            int max = Math.max(displayMetrics.widthPixels, displayMetrics.heightPixels);
            int min = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
            return String.format(Locale.US, "%dx%d", max, min);
        } else {
            return null;
        }
    }

    /**
     * Get the total memory available on the current Android device, in bytes
     */
    static long calculateTotalMemory() {
        Runtime runtime = Runtime.getRuntime();
        if (runtime.maxMemory() != Long.MAX_VALUE) {
            return runtime.maxMemory();
        } else {
            return runtime.totalMemory();
        }
    }

    /**
     * Gets information about the CPU / API
     */
    @NonNull
    String[] getCpuAbi() {
        return buildInfo.getCpuAbis();
    }

    /**
     * Get the usable disk space on internal storage's data directory
     */
    @SuppressLint("UsableSpace")
    long calculateFreeDisk() {
        // for this specific case we want the currently usable space, not
        // StorageManager#allocatableBytes() as the UsableSpace lint inspection suggests
        return dataDirectory.getUsableSpace();
    }

    /**
     * Get the amount of memory remaining that the VM can allocate
     */
    private long calculateFreeMemory() {
        Runtime runtime = Runtime.getRuntime();
        if (runtime.maxMemory() != Long.MAX_VALUE) {
            return runtime.maxMemory() - runtime.totalMemory() + runtime.freeMemory();
        } else {
            return runtime.freeMemory();
        }
    }

    /**
     * Get the device orientation, eg. "landscape"
     */
    @Nullable
    private String calculateOrientation() {
        String orientation = null;

        if (resources != null) {
            switch (resources.getConfiguration().orientation) {
                case android.content.res.Configuration.ORIENTATION_LANDSCAPE:
                    orientation = "landscape";
                    break;
                case android.content.res.Configuration.ORIENTATION_PORTRAIT:
                    orientation = "portrait";
                    break;
                default:
                    break;
            }
        }
        return orientation;
    }

    /**
     * Get the current battery charge level, eg 0.3
     */
    @Nullable
    private Float getBatteryLevel() {
        try {
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = appContext.registerReceiver(null, ifilter);

            return batteryStatus.getIntExtra("level", -1)
                / (float) batteryStatus.getIntExtra("scale", -1);
        } catch (Exception exception) {
            logger.w("Could not get batteryLevel");
        }
        return null;
    }

    /**
     * Is the device currently charging/full battery?
     */
    @Nullable
    private Boolean isCharging() {
        try {
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = appContext.registerReceiver(null, ifilter);

            int status = batteryStatus.getIntExtra("status", -1);
            return (status == BatteryManager.BATTERY_STATUS_CHARGING
                || status == BatteryManager.BATTERY_STATUS_FULL);
        } catch (Exception exception) {
            logger.w("Could not get charging status");
        }
        return null;
    }

    /**
     * Get the current status of location services
     */
    @Nullable
    @SuppressWarnings("deprecation") // LOCATION_PROVIDERS_ALLOWED is deprecated
    private String getLocationStatus() {
        try {
            ContentResolver cr = appContext.getContentResolver();
            String providersAllowed =
                Settings.Secure.getString(cr, Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            if (providersAllowed != null && providersAllowed.length() > 0) {
                return "allowed";
            } else {
                return "disallowed";
            }
        } catch (Exception exception) {
            logger.w("Could not get locationStatus");
        }
        return null;
    }

    /**
     * Get the current status of network access, eg "cellular"
     */
    @Nullable
    private String getNetworkAccess() {
        return connectivity.retrieveNetworkAccessState();
    }

    /**
     * Get the current time on the device, in ISO8601 format.
     */
    @NonNull
    private String getTime() {
        return DateUtils.toIso8601(new Date());
    }

}
