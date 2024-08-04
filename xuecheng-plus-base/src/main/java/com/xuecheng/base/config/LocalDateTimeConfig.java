package com.xuecheng.base.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author: 35238
 * 功能: 作用是统一时间格式
 * 如何使用: 只要你使用 new Date()，那么前端获取到的就是yyyy-MM-dd HH:mm:ss格式的时间字符串
 * 还有就是尽管前端传给你的是时间戳，但是你后端接收到的就是yyyy-MM-dd HH:mm:ss格式的时间字符串
 */
@Configuration
public class LocalDateTimeConfig {

    /*
     * 实体类的时间字段必须是Date类型，才生效
     * 序列化器
     *   Date -> String
     * 后端返回给前端的时间格式，会被自动处理成yyyy-MM-dd HH:mm:ss给前端
     * */
    @Bean
    public JsonSerializer<Date> dateSerializer() {
        return new JsonSerializer<Date>() {
            @Override
            public void serialize(Date date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String formattedDate = format.format(date);
                jsonGenerator.writeString(formattedDate);
            }
        };
    }

    /*
     * 实体类的时间字段必须是Date类型，才生效
     * 反序列化器
     *   Date -> String
     * 前端发送给后端的时间格式，会被自动处理成yyyy-MM-dd HH:mm:ss给后端
     * */
    @Bean
    public JsonDeserializer<Date> dateDeserializer() {
        return new JsonDeserializer<Date>() {
            @Override
            public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String date = jsonParser.getText();
                try {
                    return format.parse(date);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    //long转string避免精度损失。当后端的实体类是Long类型，传给前端时(会自动经历转String)精度丢失了
    @Bean
    public ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        //忽略value为null 时 key的输出
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        SimpleModule module = new SimpleModule();
        module.addSerializer(Long.class, ToStringSerializer.instance);
        module.addSerializer(Long.TYPE, ToStringSerializer.instance);
        objectMapper.registerModule(module);
        return objectMapper;
    }

    // 配置
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> {
            builder.serializerByType(Date.class, dateSerializer());
        };
    }

}