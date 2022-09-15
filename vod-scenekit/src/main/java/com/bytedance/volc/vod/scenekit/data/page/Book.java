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
 * Create Date : 2021/12/28
 */

package com.bytedance.volc.vod.scenekit.data.page;

import java.util.ArrayList;
import java.util.List;


public class Book<T> {
    private final List<Page<T>> pages = new ArrayList<>();
    private final int pageSize;
    private boolean end;

    public Book(int pageSize) {
        this.pageSize = pageSize;
    }

    public List<T> firstPage(Page<T> page) {
        this.end = false;
        pages.clear();
        pages.add(page);
        return page.list;
    }

    public List<T> addPage(Page<T> page) {
        pages.add(page);
        return page.list;
    }

    public boolean hasMore() {
        if (end) return false;
        if (!pages.isEmpty()) {
            Page<T> last = pages.get(pages.size() - 1);
            if (last.total == Page.TOTAL_INFINITY) {
                return last.list != null && !last.list.isEmpty() && last.list.size() >= pageSize;
            } else {
                return last.total != last.index;
            }
        }
        return false;
    }

    public int nextPageIndex() {
        if (!hasMore()) throw new IllegalStateException("has no more data!");
        Page<T> last = pages.get(pages.size() - 1);
        return last.index + 1;
    }

    public int pageSize() {
        return pageSize;
    }

    public void end() {
        this.end = true;
    }
}
