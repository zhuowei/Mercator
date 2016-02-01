#!/bin/sh
adb shell rm -r /sdcard/Mercator/unstitch/blocks
adb push textures/blocks /sdcard/Mercator/unstitch/blocks
adb shell rm -r /sdcard/Mercator/unstitch/items
adb push textures/items /sdcard/Mercator/unstitch/items
