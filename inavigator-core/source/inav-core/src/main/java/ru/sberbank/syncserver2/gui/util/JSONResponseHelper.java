package ru.sberbank.syncserver2.gui.util;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Методы для работы с JSON
 * @author sbt-gordienko-mv
 *
 */
public class JSONResponseHelper {
	
	/**
	 * Записать список объектов в формате JSON в выходной поток 
	 * @param models
	 * @param response
	 * @throws Exception
	 */
	public static <T> void listObjectToJsonOutput(List<T> models,HttpServletResponse response) throws IOException {
		response.addHeader("Content-Type", "application/json; charset=UTF-8");
		JsonGenerator jsonGenerator = new JsonFactory().createJsonGenerator(response.getWriter());
		jsonGenerator.setCodec(new ObjectMapper());
		
		jsonGenerator.writeStartObject();
		jsonGenerator.writeArrayFieldStart("data");
		if (models != null) {
			for(T object:models) {
				jsonGenerator.writeObject(object);
			}
		}
		jsonGenerator.writeEndArray();
		jsonGenerator.writeEndObject();
		jsonGenerator.flush();
	}

	/**
	 * Записать объект в формате JSON в выходной поток 
	 * @param models
	 * @param response
	 * @throws Exception
	 */
	public static <T> void singleObjectToJsonHttpOutput(T model,HttpServletResponse response) throws IOException {
		response.addHeader("Content-Type", "application/json; charset=UTF-8");
		JsonGenerator jsonGenerator = new JsonFactory().createJsonGenerator(response.getWriter());
		jsonGenerator.setCodec(new ObjectMapper());
		jsonGenerator.writeObject(model);
		jsonGenerator.flush();
	}
	
	
}
