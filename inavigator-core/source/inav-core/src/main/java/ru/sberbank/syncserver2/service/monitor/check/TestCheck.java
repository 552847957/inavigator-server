package ru.sberbank.syncserver2.service.monitor.check;

import java.util.ArrayList;
import java.util.List;

public class TestCheck extends AbstractCheckAction {

	int counter = 0;
	
	@Override
	protected List<CheckResult> doCheck() {
		List<CheckResult> results =  new ArrayList<CheckResult>();
		
		/**
		 * Если counter == 0, то проверка выдает три непройденных результата
		 * Если counter == 1, то проверка выдает, что все три непройденных результата исправились
		 * Если counter == 2, то проверка повторно выдает три непройденных результата
		 * Если counter == 3, то проверка возвращает некоторый успешный результат, который сбрасывает все ранние неуспешные проверки  
		 */
		
		if (counter == 0 || counter == 2) 
			results.add(new CheckResult("CODE1",false, "Не пройдена проверка 1"));
		else if (counter == 1)
			addSuccessfullCheckResultIfPreviousFailed(results, "CODE1","Исправлена  ошибка 1");
		

		if (counter == 0 || counter == 2) 
			results.add(new CheckResult("CODE2",false, "Не пройдена проверка 2"));
		else if (counter == 1)
			addSuccessfullCheckResultIfPreviousFailed(results, "CODE2","Исправлена  ошибка 2");

		if (counter == 0 || counter == 2) 
			results.add(new CheckResult("CODE3",false, "Не пройдена проверка 3"));
		else if (counter == 1)
			addSuccessfullCheckResultIfPreviousFailed(results, "CODE3","Исправлена  ошибка 3");
		
		
		if (counter == 3) {
			clearAllCheckResults();
			results.add(new CheckResult(true, ""));
		}
			
		counter++;
		
		
		return results;
	}
	
	@Override
	public String getDescription() {
		return "Тестовый чекер";
	}

}
