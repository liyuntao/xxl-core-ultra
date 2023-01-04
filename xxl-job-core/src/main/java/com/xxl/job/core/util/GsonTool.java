package com.xxl.job.core.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * @author xuxueli 2020-04-11 20:56:31
 */
public class GsonTool {

    private static Gson gson = null;
    static {
            gson= new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    }

    /**
     * Object 转成 json
     *
     * @param src
     * @return String
     */
    public static String toJson(Object src) {
        return gson.toJson(src);
    }

    /**
     * json 转成 特定的cls的Object
     *
     * @param json
     * @param classOfT
     * @return
     */
    public static <T> T fromJson(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }

}
