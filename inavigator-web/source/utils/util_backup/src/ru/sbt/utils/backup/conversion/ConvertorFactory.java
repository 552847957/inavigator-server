package ru.sbt.utils.backup.conversion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConvertorFactory {
	private static final String DEFAULT = "default";
	
	private static final Map<String, IConvertor> CONVERTORS = new HashMap<String, IConvertor>();
	
	static {
		registerConvertor(new DefaultConvertor());
		registerConvertor(new IntConvertor());
		registerConvertor(new ImageConvertor());
	}
	
	public static IConvertor registerConvertor(IConvertor convertor) {
		return CONVERTORS.put(convertor.getType().toLowerCase(), convertor);
	}
	
	public static IConvertor getConvertorFor(String type) {
		
		if (CONVERTORS.containsKey(type)) {
			
			return CONVERTORS.get(type);
		} else {		
			return CONVERTORS.get(DEFAULT);
		}
	}	
	
	public static IConvertor[] getConvertorFor(String[] types) {
		
		IConvertor[] convertors = new IConvertor[types.length];
		
		for (int i=0; i< types.length; i++) {
			convertors[i] = getConvertorFor(types[i]);
		}
		
		return convertors;
	}
	
	public static IConvertor[] getConvertorFor(List<String> types) {
		
		IConvertor[] convertors = new IConvertor[types.size()];
		
		for (int i=0; i< types.size(); i++) {
			convertors[i] = getConvertorFor(types.get(i));
		}
		
		return convertors;
	}
	
}
