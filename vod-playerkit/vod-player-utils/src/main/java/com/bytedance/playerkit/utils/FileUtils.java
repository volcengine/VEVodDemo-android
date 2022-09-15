/*
 * Copyright (C) 2021 bytedance
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
 * Create Date : 2021/12/27
 */

package com.bytedance.playerkit.utils;

import androidx.annotation.Nullable;

import java.io.File;
import java.util.List;

public class FileUtils {

    public static void deleteFile(File file, @Nullable List<String> suffixes) {
        if (file == null) return;
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    deleteFile(f, suffixes);
                }
            }
        } else {
            if (suffixes != null) {
                String name = file.getName();
                String suffix = name.substring(name.lastIndexOf("."));
                if (suffixes.contains(suffix)) {
                    file.delete();
                }
            } else {
                file.delete();
            }
        }
    }

    public static long getFileSize(File file) {
        return getFileSize(file, null);
    }

    public static long getFileSize(File file, @Nullable List<String> suffixes) {
        long size = 0;
        if (file == null) return size;
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    size += getFileSize(f, suffixes);
                }
            }
        } else {
            if (suffixes != null) {
                String name = file.getName();
                int index = name.lastIndexOf(".");
                if (index >= 0) {
                    String suffix = name.substring(index);
                    if (suffixes.contains(suffix)) {
                        size += file.length();
                    }
                }
            } else {
                size += file.length();
            }
        }
        return size;
    }

    public static String formatSize(long bytes) {
        if (bytes <= 0) {
            return "0";
        } else if (bytes < 1024) {
            return bytes + "B";
        } else if (bytes < 1024 * 1024) {
            return (bytes / 1024) + "KB";
        } else if (bytes < 1024 * 1024 * 1024) {
            return (bytes / 1024 / 1024) + "MB";
        } else {
            return (bytes / 1024 / 1024 / 1024) + "GB";
        }
    }
}
