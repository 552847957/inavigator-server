package ru.sberbank.syncserver2.gui.data;

import java.lang.reflect.Field;

/**
 * @author Administrator TODO To change the template for this generated type
 *         comment go to Window - Preferences - Java - Code Style - Code
 *         Templates
 */
public class SQLDescriptorRepository {

	public static <T> SQLDescriptor<T> getSQLDescriptor(Class<T> c) {
        if(c == Employee.class){
            return Employee.descriptor;
        } else {
            try {
                Thread.currentThread().setContextClassLoader(SQLDescriptorRepository.class.getClassLoader());
                Field field = c.getDeclaredField("descriptor");
                Object descriptor = field.get(null);
                return (SQLDescriptor)descriptor;
            } catch (Exception e) {
                throw new IllegalArgumentException("No descriptor found for this class [" + c + "]");
            }
        }
    }
}
