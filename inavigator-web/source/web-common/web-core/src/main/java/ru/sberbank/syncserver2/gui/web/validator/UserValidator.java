package ru.sberbank.syncserver2.gui.web.validator;

import java.util.List;

import ru.sberbank.syncserver2.gui.data.MisUser;

public class UserValidator implements AbstractValidator {


	public void validate(Object arg0, List<Error> arg1) {
		MisUser user = (MisUser)arg0;
		
		if (user.getUserName() == null || user.getUserName().equals(""))
			arg1.add(new Error("userName","Имя пользователя обязательно для заполнения",true));
		
		if (user.getIp() == null || user.getIp().equals(""))
			arg1.add(new Error("ip","IP адрес пользователя обязателен для заполнения",true));

		if (user.getEmailAD() == null || user.getEmailAD().equals(""))
			arg1.add(new Error("emailAD","Email alpha пользователя обязателен для заполнения",true));

		if (user.getEmail() == null || user.getEmail().equals(""))
			arg1.add(new Error("email","Email Sigma пользователя обязателен для заполнения",true));

		if (user.getTerrbankId() == null)
			arg1.add(new Error("terrbankId","Территориальный банк пользователя обязателен для заполнения",true));
		
	}

}
