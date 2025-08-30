#include <jni.h>
#include <linux/uinput.h>
#include <fcntl.h>
#include <unistd.h>
#include <cstring>
#include <time.h>
#include <android/log.h>

#define LOG_TAG "JoyConVirtualPad"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static void emit(int fd, uint16_t type, uint16_t code, int32_t value) {
    struct input_event ie{};
    ie.type = type;
    ie.code = code;
    ie.value = value;
    gettimeofday(&ie.time, nullptr);
    write(fd, &ie, sizeof(ie));
}

extern "C" JNIEXPORT jint JNICALL
Java_com_joyconvirtualpad_utils_NativeUinputManager_createUinputDevice(JNIEnv*, jobject) {
    int fd = open("/dev/uinput", O_WRONLY | O_NONBLOCK);
    if (fd < 0) {
        LOGE("Failed to open /dev/uinput");
        return -1;
    }

    // Кнопки
    ioctl(fd, UI_SET_EVBIT, EV_KEY);
    int buttons[] = {
        BTN_SOUTH, BTN_EAST, BTN_NORTH, BTN_WEST, // A B X Y
        BTN_TL, BTN_TR, BTN_TL2, BTN_TR2,         // L R L2 R2
        BTN_THUMBL, BTN_THUMBR,                   // стики-кнопки
        BTN_START, BTN_SELECT, BTN_MODE           // + - Home
    };
    for (int code : buttons) {
        ioctl(fd, UI_SET_KEYBIT, code);
    }

    // Оси
    ioctl(fd, UI_SET_EVBIT, EV_ABS);
    int axes[] = {ABS_X, ABS_Y, ABS_RX, ABS_RY, ABS_HAT0X, ABS_HAT0Y};
    for (int code : axes) {
        ioctl(fd, UI_SET_ABSBIT, code);
    }

    // Настройки стиков
    auto set_abs = [&](int code, int min, int max) {
        struct uinput_abs_setup abs{};
        abs.code = code;
        abs.absinfo.minimum = min;
        abs.absinfo.maximum = max;
        abs.absinfo.fuzz = 16;
        abs.absinfo.flat = 128;
        ioctl(fd, UI_ABS_SETUP, &abs);
    };
    set_abs(ABS_X, -32768, 32767);
    set_abs(ABS_Y, -32768, 32767);
    set_abs(ABS_RX, -32768, 32767);
    set_abs(ABS_RY, -32768, 32767);
    set_abs(ABS_HAT0X, -1, 1);
    set_abs(ABS_HAT0Y, -1, 1);

    // Описание виртуального девайса
    struct uinput_setup us{};
    us.id.bustype = BUS_USB;
    us.id.vendor  = 0x045e; // Microsoft
    us.id.product = 0x028e; // Xbox 360 Controller
    strcpy(us.name, "JoyCon Virtual Gamepad");

    ioctl(fd, UI_DEV_SETUP, &us);
    ioctl(fd, UI_DEV_CREATE);

    LOGI("Virtual gamepad created (fd=%d)", fd);
    return fd;
}

extern "C" JNIEXPORT void JNICALL
Java_com_joyconvirtualpad_utils_NativeUinputManager_sendKeyEvent(JNIEnv*, jobject, jint fd, jint code, jint value) {
    if (fd > 0) emit(fd, EV_KEY, code, value);
}

extern "C" JNIEXPORT void JNICALL
Java_com_joyconvirtualpad_utils_NativeUinputManager_sendAbsEvent(JNIEnv*, jobject, jint fd, jint code, jint value) {
    if (fd > 0) emit(fd, EV_ABS, code, value);
}

extern "C" JNIEXPORT void JNICALL
Java_com_joyconvirtualpad_utils_NativeUinputManager_sync(JNIEnv*, jobject, jint fd) {
    if (fd > 0) emit(fd, EV_SYN, SYN_REPORT, 0);
}

extern "C" JNIEXPORT void JNICALL
Java_com_joyconvirtualpad_utils_NativeUinputManager_destroyUinputDevice(JNIEnv*, jobject, jint fd) {
    if (fd > 0) {
        ioctl(fd, UI_DEV_DESTROY);
        close(fd);
    }
}
