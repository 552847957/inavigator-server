package ru.sberbank.syncserver2.service.log;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

public interface GeneratorLogFile {
	void generateFile(HttpServletResponse response, List<? extends Iterable<String>> list);
}
