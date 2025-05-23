/*
 * Copyright (C) 2025 bytedance
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
 * Create Date : 2025/5/22
 */

package com.bytedance.vodplayer.example.utils

import com.bytedance.applog.IDataObserver
import org.json.JSONObject

interface IDataObserverAdapter : IDataObserver {
    override fun onIdLoaded(did: String, iid: String, ssid: String) {
    }

    override fun onRemoteIdGet(changed: Boolean,
                               oldDid: String?, newDid: String,
                               oldIid: String, newIid: String,
                               oldSsid: String, newSsid: String) {}

    override fun onRemoteConfigGet(changed: Boolean, config: JSONObject?) {}

    override fun onRemoteAbConfigGet(changed: Boolean, abConfig: JSONObject) {}

    override fun onAbVidsChange(oldVid: String, newVid: String) {}
}