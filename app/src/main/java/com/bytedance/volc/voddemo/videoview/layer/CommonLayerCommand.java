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
 * Create Date : 2021/6/8
 */
package com.bytedance.volc.voddemo.videoview.layer;

public class CommonLayerCommand implements IVideoLayerCommand {

    private final int command;
    private Object params;

    public CommonLayerCommand(int command) {
        this.command = command;
    }

    public CommonLayerCommand(int command, Object params) {
        this.params = params;
        this.command = command;
    }

    public int getCommand() {
        return command;
    }

    @Override
    public <T> T getParam(Class<T> clazz) {
        if (clazz != null && clazz.isInstance(params)) {
            // noinspection unchecked
            return (T) params;
        }
        return null;
    }

    public void setParams(Object params) {
        this.params = params;
    }
}
