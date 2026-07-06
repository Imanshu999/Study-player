cmake_minimum_required(VERSION 3.22.1)

project("studycontroller_native")

# यहाँ हम सिस्टम को बता रहे हैं कि हमें 'magtsync' नाम की लाइब्रेरी बनानी है
add_library(
        magtsync
        SHARED
        magtsync.cpp
)

find_library(
        log-lib
        log
)

target_link_libraries(
        magtsync
        ${log-lib}
)
