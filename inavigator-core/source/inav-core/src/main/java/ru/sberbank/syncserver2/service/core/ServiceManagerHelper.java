package ru.sberbank.syncserver2.service.core;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.log4j.Logger;
import ru.sberbank.syncserver2.service.core.config.Bean;
import ru.sberbank.syncserver2.service.core.config.BeanProperty;
import ru.sberbank.syncserver2.service.log.TagLogger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by sbt-kozhinsky-lb on 25.02.14.
 */
public class ServiceManagerHelper {
    /**
     * @param instance
     * @param property
     * @param value
     * @throws ComponentException
     */
    public static void setProperty(Object instance, String property, Object value) throws ComponentException {
        //1. Properties equal null are not set
        if(value==null){
            return;
        }

        //2. Set property
        Class<? extends Object> instanceClass = instance.getClass();
		try {
            char first = Character.toUpperCase(property.charAt(0));
			Class<? extends Object> paramClass = value.getClass();

			String methodName = "set" + first + property.substring(1);
			Method method = MethodUtils.getMatchingAccessibleMethod(instanceClass, methodName, new Class<?>[] {paramClass});

			if (method == null) {
				methodName = "add" + first + property.substring(1);
				method = MethodUtils.getMatchingAccessibleMethod(instanceClass, methodName, new Class<?>[] {paramClass});
			}

			if (method == null) {
				throw new NoSuchMethodException("Class "
						+ instanceClass.getName() + " doesn't contain method "
						+ methodName + " for param class "
						+ paramClass.getName());
			}
            method.invoke(instance, value);
        } catch (SecurityException e) {
            throw new ComponentException("Class '" + instanceClass + "' property '" + property + "' can't be assigned", e);
        } catch (IllegalArgumentException e) {
            throw new ComponentException("Class '" + instanceClass + "' property '" + property + "' can't be assigned", e);
        } catch (NoSuchMethodException e) {
            throw new ComponentException("Class '" + instanceClass + "' property '" + property + "' can't be assigned", e);
        } catch (IllegalAccessException e) {
            throw new ComponentException("Class '" + instanceClass + "' property '" + property + "' can't be assigned", e);
        } catch (InvocationTargetException e) {
            throw new ComponentException("Class '" + instanceClass + "' property '" + property + "' can't be assigned", e);
        }
    }

    /**
     * Creates class instance specified by it's fully qualified name. Suppresses exception thrown and adds errors to context if any.
     *
     * @param name
     * @return
     * @throws ComponentException
     */
    private static Object createClassInstance(String name) throws ComponentException{
        Object instance = null;
        try {
            Class<?> clazz = Class.forName(name);
            instance = clazz.newInstance();
        } catch (ClassNotFoundException e) {
            throw new ComponentException(e);
        } catch (InstantiationException e) {
            throw new ComponentException(e);
        } catch (IllegalAccessException e) {
            throw new ComponentException(e);
        }

        return instance;
    }

    public static Object createAndConfigure(Bean bean) throws ComponentException {
        Object instance = createClassInstance(bean.getClazz());
        configure(bean, instance);
        return instance;
    }

	public static void configure(Bean bean, Object instance)
			throws ComponentException {
		List<BeanProperty> properties = bean.getBeanProperties();
        if(properties!=null){
            for (int i = 0; i < properties.size(); i++) {
                BeanProperty beanProperty =  properties.get(i);
                setProperty(instance,beanProperty.getCode(), beanProperty.getValue());
            }
        }
	}

    public static void setTagLoggerForUnitTest(AbstractService service){
        service.tagLogger = TagLogger.getTagLogger(service.getClass(), Logger.getLogger(service.getClass()), "debug");
    }
}
