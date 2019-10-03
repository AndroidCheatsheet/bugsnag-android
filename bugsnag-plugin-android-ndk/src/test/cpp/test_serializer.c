#include <utils/string.h>
#include "test_serializer.h"

enum greatest_test_res validate_serialized_json(const test_case *test_case,
                                                JSON_Value *event_val) {
    // convert to string
    char *serialized_string = json_serialize_to_string(event_val);
    json_value_free(event_val);

    // validate structure
    char *expected = test_case->expected_json;
    ASSERT_STR_EQ(expected, serialized_string);
    PASS();
}

bsg_user * loadUserTestCase(jint num) {
    bsg_user *user;

    if (num == 0) {
        user = malloc(sizeof(bsg_user));
        strcpy(user->id, "1234");
        strcpy(user->email, "fenton@io.example.com");
        strcpy(user->name, "Fenton");
    } else {
        user = malloc(sizeof(bsg_user));
        strcpy(user->id, "456");
        strcpy(user->email, "jamie@bugsnag.com");
        strcpy(user->name, "Jamie");
    }
    return user;
}

bsg_app_info * loadAppTestCase(jint num) {
    bsg_app_info *app = malloc(sizeof(bsg_app_info));
    strcpy(app->version, "22");
    strcpy(app->id, "com.bugsnag.example");
    strcpy(app->type, "android");
    strcpy(app->release_stage, "prod");
    app->version_code = 55;
    strcpy(app->build_uuid, "1234-uuid");
    strcpy(app->binaryArch, "x86");
    app->duration = 6502;
    app->duration_in_foreground = 6502;
    app->in_foreground = true;
    return app;
}

bsg_app_info * loadAppMetaDataTestCase(jint num) {
    bsg_app_info *app = malloc(sizeof(bsg_app_info));
    strcpy(app->package_name, "com.bugsnag.example");
    strcpy(app->version_name, "5.0");
    strcpy(app->active_screen, "MainActivity");
    strcpy(app->name, "PhotoSnap");
    app->low_memory = true;
    return app;
}

bsg_device_info * loadDeviceTestCase(jint num) {
    bsg_device_info *device = malloc(sizeof(bsg_device_info));
    strcpy(device->id, "f5gh7");
    strcpy(device->os_version, "8.1");
    strcpy(device->manufacturer, "Samsung");
    strcpy(device->model, "S7");
    strcpy(device->orientation, "portrait");
    strcpy(device->os_build, "BullDog 5.2");
    device->total_memory = 512340922;
    device->api_level = 29;

    bsg_strncpy_safe(device->cpu_abi[0].value, "x86", sizeof(device->cpu_abi[0].value));
    device->cpu_abi_count = 1;
    return device;
}

bsg_device_info * loadDeviceMetaDataTestCase(jint num) {
    bsg_device_info *device = malloc(sizeof(bsg_device_info));
    strcpy(device->brand, "Samsung");
    strcpy(device->locale, "En");
    strcpy(device->location_status, "cellular");
    strcpy(device->network_access, "full");
    strcpy(device->screen_resolution, "1024x768");
    device->emulator = false;
    device->jailbroken = false;
    device->dpi = 320;
    device->screen_density = 3.5;

    struct tm time = { 0, 0, 0, 1, 12, 128 };
    device->time = mktime(&time);
    return device;
}

bugsnag_metadata * loadCustomMetaDataTestCase(jint num) {
    bugsnag_metadata *data = malloc(sizeof(bugsnag_metadata));
    return data;
}

bugsnag_report * loadContextTestCase(jint num) {
    bugsnag_report *data = malloc(sizeof(bugsnag_report));
    strcpy(data->context, "CustomContext");
    strcpy(data->app.active_screen, "ExampleActivity");
    return data;
}

bugsnag_report * loadHandledStateTestCase(jint num) {
    bugsnag_report *data = malloc(sizeof(bugsnag_report));
    return data;
}
