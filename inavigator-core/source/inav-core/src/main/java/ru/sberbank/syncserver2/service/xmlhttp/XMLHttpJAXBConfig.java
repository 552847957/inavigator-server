package ru.sberbank.syncserver2.service.xmlhttp;

import ru.sberbank.syncserver2.service.core.AbstractService;

/**
 * Created by User on 27.02.15.
 */
public class XMLHttpJAXBConfig extends AbstractService {
    private String requestClassNames;
    private String responseClassNames;
    private Class[] requestClassObjects;
    private Class[] responseClassObjects;

    public String getRequestClassNames() {
        return requestClassNames;
    }

    public void setRequestClassNames(String requestClassNames) {
        this.requestClassNames = requestClassNames;
        String[] names = requestClassNames.split(";");
        this.requestClassObjects = getClassesByNames(names);
    }

    public String getResponseClassNames() {
        return responseClassNames;
    }

    public void setResponseClassNames(String responseClassNames) {
        this.responseClassNames = responseClassNames;
        String[] names = responseClassNames.split(";");
        this.responseClassObjects = getClassesByNames(names);
    }

    static Class[] getClassesByNames(String[] names){
        Class[] result = new Class[names.length];
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            try {
                Class c = Class.forName(name);
                result[i] = c;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public Class[] getRequestClasses(){
        return requestClassObjects;
    }

    public Class[] getResponseClasses(){
        return responseClassObjects;
    }

    @Override
    protected void doStop() {

    }

    @Override
    protected void waitUntilStopped() {

    }
}
