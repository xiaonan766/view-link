package com.viewlink.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class JsonUtils {
    private static final Logger logger= LoggerFactory.getLogger(JsonUtils.class);
    /*
    * Object转化为json
    * */
    public static String convertObj2Json(Object obj){
        return JSON.toJSONString(obj);
    }
    /*
    * json转化为Object*/
    public static <T> T convertJson2Obj(String json,Class<T> classz){
        return JSONObject.parseObject(json,classz);
    }
    /*
    json集合转换为list集合
    */
    public static <T> List<T> convertJsonArray2List(String json, Class<T> classz){
        return JSONArray.parseArray(json,classz);
    }
    /*main方法*/
    public static void main(String[] args) {

    }
}
