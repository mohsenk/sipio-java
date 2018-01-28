package com.fonoster.sipio.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.gson.JsonElement;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class ResourceUtil {

    JsonSchemaFactory factory = new JsonSchemaFactory();
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
        HashMap obj = this.yamlReader.readValue(yaml, HashMap.class);
        return this.mapper.writeValueAsString(obj);
    }

    public boolean isJson(String str) {
        try {
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public JsonElement getJson(String yamlFile) throws IOException {
        //return new org.().parse(getJsonString(yamlFile));
        return new com.google.gson.JsonParser().parse(getJsonString(yamlFile));
    }



//    public void getObjs(String resourcePath, String f) throws IOException {
//        String filter = "*";
//
//        if (!StringUtils.isEmpty(f) && f != "*") {
//            filter = "*.[?(" + f + ")]";
//        }
//
//        String resource = this.getJsonString(resourcePath);
//        let list
//        try {
//            // Convert this from net.minidev.json.JSONArray
//            // to Javascript Object
//            list = JSON.parse(JsonPath.parse(resource).read(filter).toJSONString())
//
//            if (list.length == 0) {
//                return {
//                        status:Status.NOT_FOUND,
//                        message:Status.message[Status.NOT_FOUND].value
//                }
//            }
//        } catch (e) {
//            return {
//                    status:Status.BAD_REQUEST,
//                    message:e.getMessage()
//            }
//        }
//
//        return {
//                status:Status.OK,
//                message:Status.message[Status.OK].value,
//                obj:list
//        }
//    }


    public boolean isResourceValid(String schemaPath, String nodePath) throws Exception {
        JsonSchema schema = this.factory.getSchema(this.readFile(schemaPath));
        JsonNode node = this.mapper.readTree(this.getJsonString(nodePath));
        Set<ValidationMessage> errors = schema.validate(node);
        Boolean result = true;
        for (ValidationMessage error : errors) {
            logger.warn("We found some errors in your resource " + node);
            result = false;
        }
        return result;
    }


}
