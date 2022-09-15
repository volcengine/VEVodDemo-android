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
 * Create Date : 2021/12/3
 */

package com.bytedance.playerkit.utils.io;

import android.os.Build;

import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;


public class IOUtils {

    public static Charset charset() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return StandardCharsets.UTF_8;
        }
        return Charset.forName("UTF-8");
    }

    public static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) closeable.close();
        } catch (IOException e) { /* ignored */ }
    }

    @Nullable
    public static String headerValue(Map<String, List<String>> headers, String name) {
        if (headers == null) return null;
        List<String> values = headers.get(name);
        if (values != null && !values.isEmpty()) {
            return values.get(values.size() - 1);
        }
        return null;
    }

    public static long parseLong(String input) {
        try {
            return Long.parseLong(input);
        } catch (NumberFormatException e) { /* ignore */ }
        return -1;
    }

    @Nullable
    public static String inputStream2String(InputStream inputStream) {
        if (inputStream == null) return null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int read;
        try {
            while ((read = inputStream.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return out.toString(charset().name());
        } catch (IOException e) {
            return null;
        }
    }
}
