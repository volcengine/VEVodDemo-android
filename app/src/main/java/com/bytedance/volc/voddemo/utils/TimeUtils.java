/*
 * Copyright 2021 bytedance
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Create Date : 2021/2/25
 */
package com.bytedance.volc.voddemo.utils;

public class TimeUtils {


    public static String secondsToTimer(int seconds) {
        StringBuilder finalTimerString = new StringBuilder();

        int minutes = seconds / 60;
        seconds = seconds % 60;

        if (minutes >= 10) {
            finalTimerString.append(minutes);
        } else if (minutes > 0) {
            finalTimerString.append(0);
            finalTimerString.append(minutes);
        } else {
            finalTimerString.append(0);
            finalTimerString.append(0);
        }
        finalTimerString.append(":");

        if (seconds >= 10) {
            finalTimerString.append(seconds);
        } else if (seconds > 0) {
            finalTimerString.append(0);
            finalTimerString.append(seconds);
        } else {
            finalTimerString.append(0);
            finalTimerString.append(0);
        }

        return finalTimerString.toString();
    }

    public static String secondsToChineseTimer(long seconds) {
        StringBuilder finalTimerString = new StringBuilder();

        long minutes = seconds / 60;
        seconds = seconds % 60;

        if (minutes >= 10) {
            finalTimerString.append(minutes);
        } else if (minutes > 0) {
            finalTimerString.append(0);
            finalTimerString.append(minutes);
        } else {
            finalTimerString.append(0);
            finalTimerString.append(0);
        }
        finalTimerString.append("分");

        if (seconds >= 10) {
            finalTimerString.append(seconds);
        } else if (seconds > 0) {
            finalTimerString.append(0);
            finalTimerString.append(seconds);
        } else {
            finalTimerString.append(0);
            finalTimerString.append(0);
        }
        finalTimerString.append("秒");

        return finalTimerString.toString();
    }

    public static String milliSecondsToTimer(long milliseconds) {
        StringBuilder finalTimerString = new StringBuilder();

        long minutes = (milliseconds) / (1000 * 60);
        long seconds = ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);

        if (minutes >= 10) {
            finalTimerString.append(minutes);
        } else if (minutes > 0) {
            finalTimerString.append(0);
            finalTimerString.append(minutes);
        } else {
            finalTimerString.append(0);
            finalTimerString.append(0);
        }
        finalTimerString.append(":");

        if (seconds >= 10) {
            finalTimerString.append(seconds);
        } else if (seconds > 0) {
            finalTimerString.append(0);
            finalTimerString.append(seconds);
        } else {
            finalTimerString.append(0);
            finalTimerString.append(0);
        }

        return finalTimerString.toString();
    }

    public static float timeToFloatPercent(long currentPosition, long duration) {
        if (currentPosition < 0) {
            currentPosition = 0;
        }
        float progress = 0f;
        if (duration > 0) {
            progress = currentPosition * 1.0f / duration * 100;
        }
        return progress;
    }

    public static int timeToPercent(long currentPosition, long duration) {
        int progress = 0;
        if (duration > 0) {
            progress = (int) (currentPosition * 1.0 / duration * 100);
        }
        return progress;
    }
}
