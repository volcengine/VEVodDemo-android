package com.bytedance.volc.voddemo.utils;

import android.text.TextUtils;

import com.bytedance.playerkit.utils.MD5;

import java.util.List;

import okhttp3.HttpUrl;

public class CacheKeyUtils {

    public static String generateVolcCDNUrlTypeCCacheKey(String url) {
        HttpUrl httpUrl = HttpUrl.parse(url);
        if (httpUrl == null) return null;
        List<String> pathSegments = httpUrl.pathSegments();
        if (pathSegments.isEmpty() || pathSegments.size() < 2) {
            return null;
        }
        String signaturePath = pathSegments.get(0);
        String expireTimePath = pathSegments.get(1);
        if (TextUtils.isEmpty(signaturePath)) {
            return null;
        }
        if (TextUtils.isEmpty(expireTimePath) || expireTimePath.length() != 8) {
            return null;
        }
        Long expireTime = null;
        try {
            expireTime = Long.valueOf(expireTimePath, 16);
        } catch (NumberFormatException ignore) {
        }
        if (expireTime == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 2; i < pathSegments.size(); i++) {
            sb.append(pathSegments.get(i));
        }
        return MD5.getMD5(sb.toString());
    }
}
