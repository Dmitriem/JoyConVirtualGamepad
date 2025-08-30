package com.joyconvirtualpad.utils

object NativeUinputManager {
    init {
        System.loadLibrary("joyconvirtualpad") // имя в CMakeLists (add_library)
    }

    @JvmStatic external fun createUinputDevice(): Int
    @JvmStatic external fun sendKeyEvent(fd: Int, code: Int, value: Int)
    @JvmStatic external fun sendAbsEvent(fd: Int, code: Int, value: Int)
    @JvmStatic external fun sync(fd: Int)
    @JvmStatic external fun destroyUinputDevice(fd: Int)
}
