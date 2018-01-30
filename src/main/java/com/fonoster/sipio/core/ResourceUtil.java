package com.fonoster.sipio.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class ResourceUtil {

    ObjectMapper mapper = new ObjectMapper();
    ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());

    static final Logger logger = LogManager.getLogger(ResourceUtil.class);

    public ResourceUtil() {

    }

    public String readFile(String path) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);
        StringBuilder stringBuilder = new StringBuilder();
        for (String line : lines) {
            stringBuilder.append(line).append("\n");
        }
        return stringBuilder.toString();
    }

    public String getJsonString(String yamlFile) throws IOException {
        String yaml = this.readFile(yamlFile);
        if (StringUtils.isEmpty(yaml)) return null;
        Object obj = this.yamlReader.readValue(yaml, Object.class);
        return this.mapper.writeValueAsString(obj);
    }

    public JsonElement getJson(String yamlFile) throws IOException {
        String json = getJsonString(yamlFile);
        if (StringUtils.isEmpty(json)) return null;
        return new JsonParser().parse(json);
    }



}
