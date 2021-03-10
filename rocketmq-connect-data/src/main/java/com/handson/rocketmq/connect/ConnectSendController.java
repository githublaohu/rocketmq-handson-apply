package com.handson.rocketmq.connect;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import io.openmessaging.connector.api.data.DataEntryBuilder;
import io.openmessaging.connector.api.data.EntryType;
import io.openmessaging.connector.api.data.Field;
import io.openmessaging.connector.api.data.FieldType;
import io.openmessaging.connector.api.data.Schema;
import io.openmessaging.connector.api.data.SinkDataEntry;
import io.openmessaging.connector.api.data.SourceDataEntry;

@RestController
@RequestMapping("/connect")
public class ConnectSendController {

	private static final Map<Class<?>, FieldType> FIELDTYPE_CLASS = new HashMap<>();

	static {
		FIELDTYPE_CLASS.put(Integer.class, FieldType.INT32);
		FIELDTYPE_CLASS.put(Long.class, FieldType.INT64);
		FIELDTYPE_CLASS.put(BigInteger.class, FieldType.BIG_INTEGER);
		FIELDTYPE_CLASS.put(Float.class, FieldType.FLOAT32);
		FIELDTYPE_CLASS.put(Double.class, FieldType.FLOAT64);
		FIELDTYPE_CLASS.put(String.class, FieldType.STRING);
		FIELDTYPE_CLASS.put(Byte.class, FieldType.BYTES);
		FIELDTYPE_CLASS.put(Boolean.class, FieldType.BOOLEAN);
	}

	@Autowired
	private RocketMQTemplate rocketMQTemplate;

	@GetMapping("/sendCreateData")
	public SendResult sendCreateData(SinkDataEntry sinkDataEntry) {
		return rocketMQTemplate.syncSend("", sinkDataEntry);
	}

	@GetMapping("/sendUpdateData")
	public SendResult sendUpdateData(SinkDataEntry sinkDataEntry) {
		return rocketMQTemplate.syncSend("", sinkDataEntry);
	}

	@GetMapping("/sendDeleteData")
	public SendResult sendDeleteData(SinkDataEntry sinkDataEntry) {
		return rocketMQTemplate.syncSend("", sinkDataEntry);
	}

	@PostMapping("/createData/{topic}/{mapper}")
	public SendResult createData(@RequestBody Map<String, Object> data, @PathVariable String topic,
			@PathVariable String mapper) throws UnsupportedEncodingException {
		Schema schema = new Schema();
		schema.setName(mapper);
		int i = 0;

		List<Field> fieldList = new ArrayList<>();
		schema.setFields(fieldList);
		for (Entry<String, Object> e : data.entrySet()) {
			Field field = new Field(i++, e.getKey(), FIELDTYPE_CLASS.get(e.getValue().getClass()));
			fieldList.add(field);
		}
		DataEntryBuilder dataEntryBuilder = new DataEntryBuilder(schema).entryType(EntryType.CREATE).queue(topic)
				.timestamp(System.currentTimeMillis());
		for (Entry<String, Object> e : data.entrySet()) {
			dataEntryBuilder.putFiled(e.getKey(), JSON.toJSONString(new Object[] { e.getValue(), "" }));
		}
		SourceDataEntry sourceDataEntry = dataEntryBuilder.buildSourceDataEntry(offsetValue(1L), offsetValue(1L));
		Object[] newPayload = new Object[1];
		newPayload[0] = Base64.getEncoder()
				.encodeToString(JSON.toJSONString(sourceDataEntry.getPayload()).getBytes("UTF-8"));
		sourceDataEntry.setPayload(newPayload);
		return rocketMQTemplate.syncSend(topic, JSON.toJSONString(sourceDataEntry).getBytes());
	}
	
	@PostMapping("/updateData/{topic}/{mapper}")
	public SendResult updateData(@RequestBody Map<String, List<Object>> data, @PathVariable String topic,
			@PathVariable String mapper) throws UnsupportedEncodingException {
		Schema schema = new Schema();
		schema.setName(mapper);
		int i = 0;
		List<Field> fieldList = new ArrayList<>();
		schema.setFields(fieldList);
		for (Entry<String,  List<Object>> e : data.entrySet()) {
			Field field = new Field(i++, e.getKey(), FIELDTYPE_CLASS.get(e.getValue().get(1).getClass()));
			fieldList.add(field);
		}
		DataEntryBuilder dataEntryBuilder = new DataEntryBuilder(schema).entryType(EntryType.UPDATE).queue(topic)
				.timestamp(System.currentTimeMillis());
		for (Entry<String,  List<Object>> e : data.entrySet()) {
			dataEntryBuilder.putFiled(e.getKey(), JSON.toJSONString(new Object[] { e.getValue().get(0),  e.getValue().get(1) }));
		}
		SourceDataEntry sourceDataEntry = dataEntryBuilder.buildSourceDataEntry(offsetValue(1L), offsetValue(1L));
		Object[] newPayload = new Object[1];
		newPayload[0] = Base64.getEncoder()
				.encodeToString(JSON.toJSONString(sourceDataEntry.getPayload()).getBytes("UTF-8"));
		sourceDataEntry.setPayload(newPayload);
		return rocketMQTemplate.syncSend(topic, JSON.toJSONString(sourceDataEntry).getBytes());
	}
	
	@PostMapping("/deleteData/{topic}/{mapper}")
	public SendResult deleteData(@RequestBody Map<String, List<Object>> data, @PathVariable String topic,
			@PathVariable String mapper) throws UnsupportedEncodingException {
		Schema schema = new Schema();
		schema.setName(mapper);
		int i = 0;
		List<Field> fieldList = new ArrayList<>();
		schema.setFields(fieldList);
		for (Entry<String,  List<Object>> e : data.entrySet()) {
			Field field = new Field(i++, e.getKey(), FIELDTYPE_CLASS.get(e.getValue().get(1).getClass()));
			fieldList.add(field);
		}
		DataEntryBuilder dataEntryBuilder = new DataEntryBuilder(schema).entryType(EntryType.DELETE).queue(topic)
				.timestamp(System.currentTimeMillis());
		for (Entry<String,  List<Object>> e : data.entrySet()) {
			dataEntryBuilder.putFiled(e.getKey(), JSON.toJSONString(new Object[] { e.getValue().get(0),  e.getValue().get(1) }));
		}
		SourceDataEntry sourceDataEntry = dataEntryBuilder.buildSourceDataEntry(offsetValue(1L), offsetValue(1L));
		Object[] newPayload = new Object[1];
		newPayload[0] = Base64.getEncoder()
				.encodeToString(JSON.toJSONString(sourceDataEntry.getPayload()).getBytes("UTF-8"));
		sourceDataEntry.setPayload(newPayload);
		return rocketMQTemplate.syncSend(topic, JSON.toJSONString(sourceDataEntry).getBytes());
	}

	private ByteBuffer offsetValue(Long pos) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("nextPosition", pos);
		return ByteBuffer.wrap(jsonObject.toJSONString().getBytes(Charset.defaultCharset()));
	}

}
