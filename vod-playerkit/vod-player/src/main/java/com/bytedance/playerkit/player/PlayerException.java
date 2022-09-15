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

package com.bytedance.playerkit.player;

public class PlayerException extends Exception {

    public static final int CODE_ERROR_ACTION = 1;
    public static final int CODE_SOURCE_LOAD_ERROR = 2;
    public static final int CODE_SOURCE_SET_ERROR = 3;

    private final int code;

    public PlayerException(int code, String message) {
        super("code:" + code + "; msg:" + message);
        this.code = code;
    }

    public PlayerException(int code, String message, Throwable cause) {
        super("code:" + code + "; msg:" + message, cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
