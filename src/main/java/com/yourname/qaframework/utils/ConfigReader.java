package com.yourname.qaframework.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {

    private static Properties properties;

    static {
        try {
            String path = "src/test/resources/config.properties";
            FileInputStream input = new FileInputStream(path);
            properties = new Properties();
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("config.properties not found at expected path", e);
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }
}