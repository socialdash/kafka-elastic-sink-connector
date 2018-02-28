package com.skynyrd.kafka.transform;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.apache.kafka.connect.json.JsonConverter;
import org.apache.kafka.connect.sink.SinkRecord;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Collections;

public abstract class AbstractRecordTransformer implements RecordTransformer {
    protected JsonConverter jsonConverter;
    protected Gson gson;

    public AbstractRecordTransformer() {
        jsonConverter = new JsonConverter();
        jsonConverter.configure(Collections.singletonMap("schemas", false), false);

        gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
                .create();
    }

    protected JsonObject extractPayload(SinkRecord record) throws ParseException {
        try {
            byte[] rawJsonPayload = jsonConverter.fromConnectData(
                    record.topic(), record.valueSchema(), record.value());
            String recordStr = new String(rawJsonPayload, StandardCharsets.UTF_8);
            JsonObject recordAsJson = gson.fromJson(recordStr, JsonObject.class);
            JsonObject payload = recordAsJson.getAsJsonObject("payload").getAsJsonObject("after");
            return payload;
        } catch (Exception e) {
            throw new ParseException("Error parsing record + " + record, -1);
        }
    }
}
