package com.bytedance.vodplayer.example.utils;

public class SpeedUtils {

    public static String format(long speedInBytePerSecond) {
        if (speedInBytePerSecond < 0L) {
            return null;
        } else if (speedInBytePerSecond < 1024) {
            return String.format("%s%s", speedInBytePerSecond, "B/S");
        } else if (speedInBytePerSecond < 1024 * 1024) {
            return String.format("%s%s", ((int) ((speedInBytePerSecond / 1024f) * 10)) / 10f, "KB/S");
        } else {
            return String.format("%s%s", ((int) ((speedInBytePerSecond / 1024f / 1024f) * 10)) / 10f, "MB/S");
        }
    }
}
