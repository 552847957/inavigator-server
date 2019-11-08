package ru.sberbank.syncserver2.xstream;

/**
 * Constance for XML parsers.
 */
public class XmlConstants {
    /**
     * Закрытый конструктор, предотвращающий создание объектов этого класса.
     */
    private XmlConstants() {
    }

    // константы, представляющие собой значения, подставляемые вместо null.

    /**
     * Номер версии схемы CommerceML.
     */
    public static final String SCHEMA_VER = "2.021";

    /**
     * Номер версии схемы EZakazXML.
     */
    public static final String EZAKAZ_SCHEMA_VER = "1.0.0";

    /**
     * Если значение даты равно null, то подставляется это значение.
     */
    public static final String EMPTY_DATE = "-9999-12-31";

    /**
     * Общий формат времению.
     */
    public static final String EMPTY_TIME = "00:00:00";

    /**
     * Если значение даты-времени равно null, то подставляется это значение.
     */
    public static final String EMPTY_DATE_TIME = "0000-00-00T00:00:00";

    /**
     * Если значение строки равно null, то подставляется это значение.
     */
    public static final String EMPTY_STRING = " ";

    /**
     * Если Integer значение равно null, то подставляется это значение.
     */
    public static final String EMPTY_INTEGER_AS_STRING = "-0";

    /**
     * Если Boolean значение равно null, то подставляется это значение.
     */
    public static final String EMPTY_BOOLEAN_AS_STRING = "0";

    // другие константы

    /**
     * Общий формат даты для работы с XML.
     */
    public static final String XML_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    /**
     * Формат для имен файлов.
     */
    public static final String POSTFIX_DATE_TIME_FORMAT = "yyyyMMddHHmmss";

    /**
     * Общий формат даты для работы с XML.
     */
    public static final String LAST_UPDATE_XML_DATE_TIME_FORMAT = "dd.MM.yyyy HH:mm:ss";

    /**
     * Общий краткий формат даты для работы с XML.
     */
    public static final String XML_DATE_FORMAT = "yyyy-MM-dd";

    /**
     * Общий формат времени для работы с XML.
     */
    public static final String XML_TIME_FORMAT = "HH:mm:ss";


    /**
     * Десятичный разделитель.
     */
    public static final char XML_DECIMAL_SEPARATOR = '.';

    /**
     * Формат числового вывода с плавающей точкой с 2 нулями после запятой.
     */
    public static final String XML_NUMBER_FORMAT_1 = "###########0.00";

    /**
     * Формат числового вывода с плавающей точкой с 4 нулями после запятой.
     */
    public static final String XML_NUMBER_FORMAT_2 = "###########0.0000";

}
