package ru.sberbank.syncserver2.xstream;

/**
 * Represantation of XML attribute.
 */
public class XmlAttribute {

    /**
     * Имя параметра.
     */
    private String name;
    /**
     * Значение параметра.
     */
    private Object value;
    /**
     * Тип параметра.
     */
    private Class type;

    /**
     * Конструктор xml атрибутов.
     *
     * @param theName  имя арибута.
     * @param theValue значение аритбута.
     * @param theType  тип аотрибута.
     */
    public XmlAttribute(
            final String theName,
            final Object theValue,
            final Class theType) {
        name = theName;
        value = theValue;
        type = theType;
    }

    /**
     * Имя параметра.
     *
     * @return Имя параметра.
     */
    public String getName() {
        return name;
    }

    /**
     * Имя параметра.
     *
     * @param theName Имя параметра.
     */
    public void setName(final String theName) {
        name = theName;
    }

    /**
     * Значение параметра.
     *
     * @return Значение параметра.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Значение параметра.
     *
     * @param theValue Значение параметра.
     */
    public void setValue(final Object theValue) {
        value = theValue;
    }

    /**
     * Тип параметра.
     *
     * @return Тип параметра.
     */
    public Class getType() {
        return type;
    }

    /**
     * Тип параметра.
     *
     * @param theType Тип параметра.
     */
    public void setType(final Class theType) {
        type = theType;
    }
}
