//package com.mbc.mobileapp.utils;
//
//import com.jayway.jsonpath.Configuration;
//import com.jayway.jsonpath.JsonPath;
//import com.jayway.jsonpath.Option;
//
//import java.util.Objects;
//
//public class JsonPathUtils {
//
//    private static Configuration configuration;
//
//    public static Configuration getConfiguration() {
//        if (Objects.nonNull(configuration)) {
//            return configuration;
//        }
//        configuration = Configuration.builder()
//                .options(Option.DEFAULT_PATH_LEAF_TO_NULL, Option.SUPPRESS_EXCEPTIONS)
//                .build();
//        return configuration;
//    }
//
//    public static <T> T getPropertiesByJsonPath(String json, String path, Class<T> clazz) {
//        return JsonPath.parse(json, getConfiguration()).read(path, clazz);
//    };
//}
