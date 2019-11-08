package ru.sberbank.syncserver2.gui.web.validator;

import java.util.List;

public interface AbstractValidator {

	public abstract void validate(Object arg0, List<Error> arg1);

}