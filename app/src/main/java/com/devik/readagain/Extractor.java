package com.devik.readagain;

import android.text.TextUtils;
import android.webkit.URLUtil;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by naver on 2016. 6. 13..
 */
public class Extractor {

    public static String getUrl(String text) {
        if (TextUtils.isEmpty(text)) {
            return null;
        }

        StringBuffer sb = new StringBuffer();
        String regex = "(https?):\\/\\/([^:\\/\\s]+)(:([^\\/]*))?((\\/[^\\s/\\/]+)*)?\\/([^#\\s\\?]*)(\\?([^#\\s]*))?(#(\\w*))?$";

        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(text);

        if (m.find()) {
            sb.append(m.group(0));
            System.out.println("===" + m.group(0));
        }

        String url = sb.toString();
        if (URLUtil.isNetworkUrl(url)) {
            return url;
        }
        return null;
    }
}
