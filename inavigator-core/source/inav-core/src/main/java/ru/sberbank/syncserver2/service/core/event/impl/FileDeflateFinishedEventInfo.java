package ru.sberbank.syncserver2.service.core.event.impl;

import ru.sberbank.syncserver2.service.core.event.AbstractEventInfo;
import ru.sberbank.syncserver2.service.file.transport.DeflaterInflaterRunParamHolder;

public class FileDeflateFinishedEventInfo extends AbstractEventInfo {

	private DeflaterInflaterRunParamHolder deflaterInflaterRunParamHolder;
	private String dataFileName;
	
	
	
	public FileDeflateFinishedEventInfo(
			DeflaterInflaterRunParamHolder deflaterInflaterRunParamHolder,
			String dataFileName) {
		super();
		this.deflaterInflaterRunParamHolder = deflaterInflaterRunParamHolder;
		this.dataFileName = dataFileName;
	}
	
	public DeflaterInflaterRunParamHolder getDeflaterInflaterRunParamHolder() {
		return deflaterInflaterRunParamHolder;
	}
	public void setDeflaterInflaterRunParamHolder(
			DeflaterInflaterRunParamHolder deflaterInflaterRunParamHolder) {
		this.deflaterInflaterRunParamHolder = deflaterInflaterRunParamHolder;
	}
	public String getDataFileName() {
		return dataFileName;
	}
	public void setDataFileName(String dataFileName) {
		this.dataFileName = dataFileName;
	}
	
	
}
