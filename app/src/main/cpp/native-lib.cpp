#include <jni.h>
#include <fcntl.h>
#include <unistd.h>
#include <linux/uinput.h>
#include <string.h>
#include <time.h>
#include <errno.h>

static int ufd = -1;

static int write_uinput(int fd, int type, int code, int value) {
    struct input_event ie;
    memset(&ie, 0, sizeof(ie));

    // CLOCK_MONOTONIC -> timespec (fix для NDK: используем clock_gettime в timespec)
    struct timespec ts;
    clock_gettime(CLOCK_MONOTONIC, &ts);
    ie.time.tv_sec  = ts.tv_sec;
    ie.time.tv_usec = ts.tv_nsec / 1000;

    ie.type = type;
    ie.code = code;
    ie.value = value;
    return write(fd, &ie, sizeof(ie));
}

extern "C" JNIEXPORT jint JNICALL
Java_com_joyconvirtualpad_utils_NativeUinputManager_createUinputDevice(JNIEnv*, jclass) {
    int fd = open("/dev/uinput", O_WRONLY | O_NONBLOCK);
    if (fd < 0) {
        __android_log_print(ANDROID_LOG_ERROR, "NativeUinput", "open /dev/uinput failed: %s", strerror(errno));
        return -1;
    }

    // Разрешаем типы событий
    ioctl(fd, UI_SET_EVBIT, EV_KEY);
    ioctl(fd, UI_SET_EVBIT, EV_ABS);
    ioctl(fd, UI_SET_EVBIT, EV_SYN);

    // ---- Кнопки геймпада (Xbox layout) ----
    int keys[] = {
        BTN_SOUTH, BTN_EAST, BTN_NORTH, BTN_WEST, // A,B,X,Y
        BTN_TL, BTN_TR,                           // LB,RB
        BTN_TL2, BTN_TR2,                         // LT,RT (цифрово, т.к. Joy-Con дают цифровые - 0/1)
        BTN_SELECT, BTN_START,                    // Back, Start
        BTN_MODE,                                 // Guide
        BTN_THUMBL, BTN_THUMBR                    // L3, R3
    };
    for (int k : keys) ioctl(fd, UI_SET_KEYBIT, k);

    // ---- Оси стиков ----
    // Левый стик
    ioctl(fd, UI_SET_ABSBIT, ABS_X);
    ioctl(fd, UI_SET_ABSBIT, ABS_Y);
    // Правый стик
    ioctl(fd, UI_SET_ABSBIT, ABS_RX);
    ioctl(fd, UI_SET_ABSBIT, ABS_RY);
    // D-Pad как HAT
    ioctl(fd, UI_SET_ABSBIT, ABS_HAT0X);
    ioctl(fd, UI_SET_ABSBIT, ABS_HAT0Y);

    struct uinput_setup usetup;
    memset(&usetup, 0, sizeof(usetup));

    // Идентитификаторы (можно оставить любые)
    usetup.id.bustype = BUS_USB;
    usetup.id.vendor  = 0x045e; // Microsoft (для маскировки)
    usetup.id.product = 0x028e; // Xbox 360 Controller
    usetup.id.version = 0x110;

    // Название устройства
    const char* name = "Xbox 360 Controller";
    strncpy(usetup.name, name, UINPUT_MAX_NAME_SIZE - 1);

    // Настройки диапазонов осей
    // Аналоговые стики Joy-Con: -32767..32767
    struct uinput_abs_setup abs;
    memset(&abs, 0, sizeof(abs));

    auto set_abs = [&](int code, int min, int max, int flat, int fuzz){
        abs.code = code;
        abs.absinfo.value = 0;
        abs.absinfo.minimum = min;
        abs.absinfo.maximum = max;
        abs.absinfo.flat = flat;
        abs.absinfo.fuzz = fuzz;
        ioctl(fd, UI_ABS_SETUP, &abs);
    };

    set_abs(ABS_X,     -32767, 32767, 1500, 250);
    set_abs(ABS_Y,     -32767, 32767, 1500, 250);
    set_abs(ABS_RX,    -32767, 32767, 1500, 250);
    set_abs(ABS_RY,    -32767, 32767, 1500, 250);
    // HAT — дискретные значения: -1, 0, 1
    set_abs(ABS_HAT0X, -1, 1, 0, 0);
    set_abs(ABS_HAT0Y, -1, 1, 0, 0);

    if (ioctl(fd, UI_DEV_SETUP, &usetup) < 0) { close(fd); return -1; }
    if (ioctl(fd, UI_DEV_CREATE) < 0)         { close(fd); return -1; }

    ufd = fd;
    return ufd;
}

extern "C" JNIEXPORT void JNICALL
Java_com_joyconvirtualpad_utils_NativeUinputManager_sendKeyEvent(JNIEnv*, jclass, jint fd, jint code, jint value) {
    if (fd <= 0) return;
    write_uinput(fd, EV_KEY, code, value);
}

extern "C" JNIEXPORT void JNICALL
Java_com_joyconvirtualpad_utils_NativeUinputManager_sendAbsEvent(JNIEnv*, jclass, jint fd, jint code, jint value) {
    if (fd <= 0) return;
    write_uinput(fd, EV_ABS, code, value);
}

extern "C" JNIEXPORT void JNICALL
Java_com_joyconvirtualpad_utils_NativeUinputManager_sync(JNIEnv*, jclass, jint fd) {
    if (fd <= 0) return;
    write_uinput(fd, EV_SYN, SYN_REPORT, 0);
}

extern "C" JNIEXPORT void JNICALL
Java_com_joyconvirtualpad_utils_NativeUinputManager_destroyUinputDevice(JNIEnv*, jclass, jint fd) {
    if (fd > 0) {
        ioctl(fd, UI_DEV_DESTROY);
        close(fd);
    }
    ufd = -1;
}
