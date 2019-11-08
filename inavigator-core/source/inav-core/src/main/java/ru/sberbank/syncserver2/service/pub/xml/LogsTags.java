package ru.sberbank.syncserver2.service.pub.xml;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import ru.sberbank.syncserver2.service.log.LogAction;


@XmlRootElement(name = "getLogTags")
public class LogsTags extends Message{	
	
	private List<String> tags;
	
	@XmlElementWrapper(name="tags")
	@XmlElement
	public List<String> getTags() {
		return tags;
	}	

	public void setTags(List<String> tags) {
		this.tags = tags;
	}	

}
