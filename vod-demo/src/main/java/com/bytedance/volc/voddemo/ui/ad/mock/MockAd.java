/*
 * Copyright (C) 2024 bytedance
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
 * Create Date : 2024/10/18
 */

package com.bytedance.volc.voddemo.ui.ad.mock;

import com.bytedance.volc.voddemo.ui.ad.api.Ad;
/**
 * Mock impl of Ad
 */
@Deprecated
public class MockAd implements Ad {
    private String id;
    private int type;
    private String codeId;
    private Object ad;

    public MockAd(String id, int type, String codeId, Object ad) {
        this.id = id;
        this.type = type;
        this.codeId = codeId;
        this.ad = ad;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public int type() {
        return type;
    }

    @Override
    public <T> T get() {
        return (T) ad;
    }
}
