#include <jni.h>
#include <string>
#include <android/log.h>
#include <linux/uinput.h>
#include <fcntl.h>
#include <unistd.h>

#define LOG_TAG "JoyConNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT jint JNICALL
Java_com_joyconvirtualpad_utils_NativeUinputManager_createUinputDevice(JNIEnv *env, jobject thiz) {
    int fd = open("/dev/uinput", O_WRONLY | O_NONBLOCK);
    if (fd < 0) {
        LOGE("Error opening uinput device");
        return -1;
    }

    // Enable key events
    ioctl(fd, UI_SET_EVBIT, EV_KEY);
    for (int i = BTN_JOYSTICK; i <= BTN_THUMBR; i++) {
        ioctl(fd, UI_SET_KEYBIT, i);
    }

    // Enable absolute axes
    ioctl(fd, UI_SET_EVBIT, EV_ABS);
    ioctl(fd, UI_SET_ABSBIT, ABS_X);
    ioctl(fd, UI_SET_ABSBIT, ABS_Y);
    ioctl(fd, UI_SET_ABSBIT, ABS_Z);
    ioctl(fd, UI_SET_ABSBIT, ABS_RZ);
    ioctl(fd, UI_SET_ABSBIT, ABS_HAT0X);
    ioctl(fd, UI_SET_ABSBIT, ABS_HAT0Y);

    // Setup device
    struct uinput_setup usetup = {};
    usetup.id.bustype = BUS_USB;
    usetup.id.vendor = 0x045e;  // Microsoft
    usetup.id.product = 0x028e; // Xbox 360 Controller
    strcpy(usetup.name, "Xbox 360 Wireless Receiver");

    if (ioctl(fd, UI_DEV_SETUP, &usetup) < 0) {
        LOGE("Error setting up uinput device");
        close(fd);
        return -1;
    }

    if (ioctl(fd, UI_DEV_CREATE) < 0) {
        LOGE("Error creating uinput device");
        close(fd);
        return -1;
    }

    LOGI("Uinput device created successfully");
    return fd;
}

extern "C" JNIEXPORT void JNICALL
Java_com_joyconvirtualpad_utils_NativeUinputManager_sendKeyEvent(JNIEnv *env, jobject thiz, jint fd, jint code, jint value) {
    struct input_event ev = {};
    ev.type = EV_KEY;
    ev.code = code;
    ev.value = value;
    write(fd, &ev, sizeof(ev));

    struct input_event syn = {};
    syn.type = EV_SYN;
    syn.code = SYN_REPORT;
    syn.value = 0;
    write(fd, &syn, sizeof(syn));
}

extern "C" JNIEXPORT void JNICALL
Java_com_joyconvirtualpad_utils_NativeUinputManager_destroyUinputDevice(JNIEnv *env, jobject thiz, jint fd) {
    ioctl(fd, UI_DEV_DESTROY);
    close(fd);
    LOGI("Uinput device destroyed");
}
