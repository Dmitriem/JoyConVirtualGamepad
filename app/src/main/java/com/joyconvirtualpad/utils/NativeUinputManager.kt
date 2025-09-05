package com.joyconvirtualpad.utils

object NativeUinputManager {
    init {
        System.loadLibrary("joyconvirtualpad")
    }
    external fun createUinputDevice(): Int
    external fun sendKeyEvent(fd: Int, code: Int, value: Int)
    external fun sendAbsEvent(fd: Int, code: Int, value: Int)
    external fun sync(fd: Int)
    external fun destroyUinputDevice(fd: Int)
}
