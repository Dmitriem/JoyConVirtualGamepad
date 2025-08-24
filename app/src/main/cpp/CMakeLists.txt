cmake_minimum_required(VERSION 3.18.1)

project("joyconvirtualpad")

add_library(joyconvirtualpad SHARED
    native-lib.cpp)

target_link_libraries(joyconvirtualpad
    android
    log)
