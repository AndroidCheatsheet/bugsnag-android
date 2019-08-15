package com.bugsnag.android.mazerunner.scenarios;

import android.content.Context;
import android.os.Handler;

import com.bugsnag.android.Bugsnag;
import com.bugsnag.android.Configuration;

import androidx.annotation.NonNull;

public class CXXStopSessionScenario extends Scenario {
    static {
        System.loadLibrary("bugsnag-ndk");
        System.loadLibrary("monochrome");
        System.loadLibrary("entrypoint");
    }

    private Handler handler = new Handler();

    public native int crash(int counter);

    public CXXStopSessionScenario(@NonNull Configuration config, @NonNull Context context) {
        super(config, context);
        config.setAutoCaptureSessions(false);
    }

    @Override
    public void run() {
        super.run();
        String metadata = getEventMetaData();

        if (metadata == null || !metadata.equals("non-crashy")) {
            Bugsnag.getClient().startSession();
            Bugsnag.getClient().stopSession();

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    crash(0);
                }
            }, 8000);
        }
    }
}
