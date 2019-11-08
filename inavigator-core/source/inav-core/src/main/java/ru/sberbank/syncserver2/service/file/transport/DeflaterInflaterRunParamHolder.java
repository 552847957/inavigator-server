package ru.sberbank.syncserver2.service.file.transport;

import java.io.File;

public class DeflaterInflaterRunParamHolder {
	private File src;
	
	private File dest;
	
	public DeflaterInflaterRunParamHolder(File src, File dest) {
		super();
		this.src = src;
		this.dest = dest;
	}

	public File getSrc() {
		return src;
	}

	public void setSrc(File src) {
		this.src = src;
	}

	public File getDest() {
		return dest;
	}

	public void setDest(File dest) {
		this.dest = dest;
	}
	
	
}
