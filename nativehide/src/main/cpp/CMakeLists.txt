cmake_minimum_required(VERSION 3.4.1)
set(CMAKE_CXX_STANDARD 14)


message("Building ${LIB_NAME} for Android(${CMAKE_SYSTEM_PROCESSOR})...")

set(LIBRARY_NAME ${LIB_NAME})


set(HIDDEN_NATIVE_SOURCE
        hide.cpp
        wrap.cpp
        pmparser.c
        MapsHideHandler.cpp
        jni.cpp
        )

include_directories(./)


add_definitions(-DCORE_SO_NAME="${LIBRARY_NAME}")
set(CXX_APPEND_FLAGS "-funwind-tables -g -Wgcc-compat")

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} ${CXX_APPEND_FLAGS}")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++0x ${CXX_APPEND_FLAGS}")
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -s -Wl,--gc-sections")

add_library(${LIBRARY_NAME} SHARED ${HIDDEN_NATIVE_SOURCE})

target_link_libraries(${LIBRARY_NAME} log)