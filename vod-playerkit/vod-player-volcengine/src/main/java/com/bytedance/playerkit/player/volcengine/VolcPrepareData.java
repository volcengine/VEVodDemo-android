/*
 * Copyright (C) 2023 bytedance
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
 * Create Date : 2023/6/20
 */

package com.bytedance.playerkit.player.volcengine;

import com.ss.ttvideoengine.SubDesInfoModel;
import com.ss.ttvideoengine.strategy.source.StrategySource;

import java.util.Map;

public class VolcPrepareData {
    public final StrategySource strategySource;
    public final String subtitleAuthToken;
    public SubDesInfoModel subtitleModel;
    public int subtitleId;
    public Map<String, String> headers;

    public VolcPrepareData(StrategySource strategySource, String subtitleAuthToken, Map<String, String> headers) {
        this.strategySource = strategySource;
        this.subtitleAuthToken = subtitleAuthToken;
        this.headers = headers;
    }

    public VolcPrepareData(StrategySource strategySource, SubDesInfoModel subtitleModel, int subtitleId,  Map<String, String> headers) {
        this.strategySource = strategySource;
        this.subtitleId = subtitleId;
        this.subtitleModel = subtitleModel;
        this.subtitleAuthToken = null;
        this.headers = headers;
    }
}
