package kr.hyosang.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Hyosang on 2015-12-18.
 */
public class Cookie {
    private static Cookie mInstance = new Cookie();

    private HashMap<String, String> mCookies = new HashMap<String, String>();

    private Cookie() {
        //block default constructor

    }

    public static Cookie getInstance() {
        return mInstance;
    }

    public void add(String setcookie) {
        int idx = setcookie.indexOf(";");
        if(idx > 0) {
            String c = setcookie.substring(0, idx);

            idx = c.indexOf("=");

            if(idx > 0) {
                String key = c.substring(0, idx);
                String value = c.substring(idx + 1);

                mCookies.put(key, value);
            }
        }
    }

    public String getCookie() {
        StringBuffer sb = new StringBuffer();
        for(Map.Entry<String, String> entry : mCookies.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append(";");
        }

        return sb.toString();
    }
}
