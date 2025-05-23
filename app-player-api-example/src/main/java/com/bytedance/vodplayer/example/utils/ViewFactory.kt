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
 * Create Date : 2025/5/29
 */

package com.bytedance.vodplayer.example.utils

import android.content.Context
import android.util.TypedValue
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.content.ContextCompat.getColorStateList
import com.bytedance.vodplayer.example.R

object ViewFactory {

    fun button(context: Context, text: CharSequence, selected: Boolean): Button {
        return Button(context).apply {
            setTextColor(getColorStateList(context, R.color.vevod_api_example_button_text))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
            this.text = text
            this.isSelected = selected
        }
    }
}