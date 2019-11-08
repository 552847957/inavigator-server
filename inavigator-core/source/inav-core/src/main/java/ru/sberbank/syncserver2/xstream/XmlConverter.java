package ru.sberbank.syncserver2.xstream;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Time;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * XStream XML converter.
 */
public class XmlConverter {

    /**
     * Бизнес-лог.
     */
    protected Log businessLog = LogFactory.getLog(this.getClass());

    /**
     * Врайтер.
     */
    private HierarchicalStreamWriter writer;

    /**
     * Ридер.
     */
    private HierarchicalStreamReader reader;

    /**
     * Форматер дат со временем.
     */
    private ThreadLocal<SimpleDateFormat> xmlDateTimeFormat;

    /**
     * Форматер дат.
     */
    private ThreadLocal<SimpleDateFormat> xmlDateFormat;

    /**
     * Форматер времени.
     */
    private ThreadLocal<SimpleDateFormat> xmlTimeFormat;

    /**
     * Формат Double 1.
     */
    private ThreadLocal<DecimalFormat> xmlNumberFormat1;

    /**
     * Формат Double 2.
     */
    private ThreadLocal<DecimalFormat> xmlNumberFormat2;

    /**
     * Контекст врайтера.
     */
    private MarshallingContext mContext;

    /**
     * Контекст ридера.
     */
    private UnmarshallingContext uContext;

    /**
     * Флаг, cчитывать ли следующий элемент из иерархического ридера.
     */
    private Boolean readMoreFlag = true;
    /**
     * Ошибка при кодировании изображения в Jpeg.
     */
    private static final String JPEG_ENCODE_ERROR = "Ошибка при кодировании изображения в Jpeg";
    /**
     * Ошибка при кодировании изображения в Bmp.
     */
    private static final String BMP_ENCODE_ERROR = "Ошибка при кодировании изображения в Bmp";
    /**
     * Ошибка при кодировании изображения в Base64.
     */
    private static final String BASE64_ERROR =
            "Ошибка при (де)кодировании изображения (из)в Base64";
    /**
     * Кодировка.
     */
    private static final String CHARSET_NAME = "iso8859-1";

    /**
     * А, собственно, конструктор.
     */
    public XmlConverter() {
        xmlNumberFormat1 = new ThreadLocal<DecimalFormat>() {
            @Override
            protected DecimalFormat initialValue() {
                DecimalFormatSymbols sym = new DecimalFormatSymbols();
                sym.setDecimalSeparator('.');
                return new DecimalFormat(XmlConstants.XML_NUMBER_FORMAT_1, sym);
            }
        };
        xmlNumberFormat2 = new ThreadLocal<DecimalFormat>() {
            @Override
            protected DecimalFormat initialValue() {
                DecimalFormatSymbols sym = new DecimalFormatSymbols();
                sym.setDecimalSeparator('.');
                return new DecimalFormat(XmlConstants.XML_NUMBER_FORMAT_2, sym);
            }
        };
        xmlDateTimeFormat = new ThreadLocal<SimpleDateFormat>() {
            @Override
            protected SimpleDateFormat initialValue() {
                SimpleDateFormat dateFormat =
                        new SimpleDateFormat(XmlConstants.XML_DATE_TIME_FORMAT);
                dateFormat.setLenient(false);
                return dateFormat;
            }
        };
        xmlDateFormat = new ThreadLocal<SimpleDateFormat>() {
            @Override
            protected SimpleDateFormat initialValue() {
                SimpleDateFormat dateFormat = new SimpleDateFormat(XmlConstants.XML_DATE_FORMAT);
                dateFormat.setLenient(false);
                return dateFormat;
            }
        };
        xmlTimeFormat = new ThreadLocal<SimpleDateFormat>() {
            @Override
            protected SimpleDateFormat initialValue() {
                SimpleDateFormat dateFormat = new SimpleDateFormat(XmlConstants.XML_TIME_FORMAT);
                dateFormat.setLenient(false);
                return dateFormat;
            }
        };
    }

    /**
     * Инициализация.
     */
    public void init() {
        readMoreFlag = true;
    }

    /**
     * Возвращает дату в виде строки '2002-12-14T12:01:23'.
     *
     * @param theValue Дата для конвертации в строку формата '2002-12-14T12:01:23'.
     * @return строка формата '2002-12-14T12:01:23'.
     */
    public String toXMLDateTime(final Date theValue) {
        String result;
        if (theValue != null) {
            result = xmlDateTimeFormat.get().format(theValue.getTime());
        } else {
            result = XmlConstants.EMPTY_DATE_TIME;
        }
        return result;
    }

    /**
     * Возвращает дату из строки '2002-12-14T12:01:23'.
     *
     * @param theValue Строка формата '2002-12-14T12:01:23'.
     * @return дата '2002-12-14T12:01:23'.
     * @throws java.text.ParseException ошибка парсера.
     */
    public Date fromXMLDateTime(final String theValue) throws ParseException {
        Date result;
        if ((theValue != null) && !theValue.equals(XmlConstants.EMPTY_DATE_TIME)) {
            result = xmlDateTimeFormat.get().parse(theValue);
        } else {
            result = null;
        }
        return result;
    }

    /**
     * Возвращает дату в виде строки 'yyyy-MM-dd'.
     *
     * @param theValue Дата для конвертации в строку формата 'yyyy-MM-dd'.
     * @return строка формата 'yyyy-MM-dd'.
     */
    public String toXmlDate(final Date theValue) {
        String result;
        if (theValue != null) {
            result = xmlDateFormat.get().format(theValue.getTime());
        } else {
            result = XmlConstants.EMPTY_DATE;
        }
        return result;
    }

    /**
     * Возвращает дату из строки 'yyyy-MM-dd'.
     *
     * @param theValue Дата для конвертации из строки формата 'yyyy-MM-dd'.
     * @return дата.
     * @throws java.text.ParseException ошибка парсера.
     */
    public Date fromXmlDate(final String theValue) throws ParseException {
        Date result;
        if ((theValue != null) && !theValue.equals(XmlConstants.EMPTY_DATE)) {
            result = xmlDateFormat.get().parse(theValue);
        } else {
            result = null;
        }
        return result;
    }

    /**
     * Возвращает время в виде строки '12:58:04'.
     *
     * @param theValue Дата для конвертации в строку формата '12:58:04'.
     * @return строка формата '12:58:04'.
     */
    public String toXmlTime(final Date theValue) {
        String result;
        if (theValue != null) {
            result = xmlTimeFormat.get().format(theValue.getTime());
        } else {
            result = XmlConstants.EMPTY_TIME;
        }
        return result;
    }

    /**
     * Возвращает время из строки '12:58:04'.
     *
     * @param theValue Дата для конвертации из строки формата '12:58:04'.
     * @return дата формата '12:58:04'.
     * @throws java.text.ParseException ошибка парсера.
     */
    public Date fromXmlTime(final String theValue) throws ParseException {
        Date result;
        if ((theValue != null) && !theValue.equals(XmlConstants.EMPTY_TIME)) {
            result = xmlTimeFormat.get().parse(theValue);
        } else {
            result = null;
        }
        return result;
    }

    /**
     * Возвращает Double в виде строки '00000000.00'.
     *
     * @param theValue Дабл для конвертации в строку формата '00000000.00'.
     * @return строка формата '00000000.00'.
     */
    public String toXMLNumber(final Double theValue) {
        String result;
        if (theValue != null) {
            result = xmlNumberFormat1.get().format(theValue);
        } else {
            result = XmlConstants.EMPTY_INTEGER_AS_STRING;
        }
        return result;
    }

    /**
     * Возвращает Double из строки '00000000.00'.
     *
     * @param theValue Дабл для конвертации из строки формата '00000000.00'.
     * @return Double формата '00000000.00'.
     * @throws java.text.ParseException ошибка парсера.
     */
    public Double fromXMLNumber(final String theValue) throws ParseException {
        Double result;
        if ((theValue != null) && !theValue.equals(XmlConstants.EMPTY_INTEGER_AS_STRING)) {
            result = xmlNumberFormat1.get().parse(theValue).doubleValue();
        } else {
            result = null;
        }
        return result;
    }

    /**
     * Возвращает Double в виде строки '00000000.0000'.
     *
     * @param theValue Дабл для конвертации в строку формата '00000000.0000'.
     * @return строка формата '00000000.0000'.
     */
    public String toXMLDecimal(final Double theValue) {
        String result;
        if (theValue != null) {
            result = xmlNumberFormat2.get().format(theValue);
        } else {
            result = XmlConstants.EMPTY_INTEGER_AS_STRING;
        }
        return result;
    }

    /**
     * Возвращает Double из строки '00000000.0000'.
     *
     * @param theValue Дабл для конвертации из строки формата '00000000.0000'.
     * @return Double формата '00000000.0000'.
     * @throws java.text.ParseException ошибка парсера.
     */
    public Double fromXMLDecimal(final String theValue) throws ParseException {
        Double result;
        if ((theValue != null) && !theValue.equals(XmlConstants.EMPTY_INTEGER_AS_STRING)) {
            result = xmlNumberFormat2.get().parse(theValue).doubleValue();
        } else {
            result = null;
        }
        return result;
    }

    /**
     * Возвращает Integer в строковам формате.
     *
     * @param theValue целое число для конвертации.
     * @return строковое представление целого числа.
     */
    public String toXMLInteger(final Integer theValue) {
        String result;
        if (theValue != null) {
            result = String.valueOf(theValue);
        } else {
            result = XmlConstants.EMPTY_INTEGER_AS_STRING;
        }
        return result;
    }

    /**
     * Возвращает Integer в строковам формате.
     *
     * @param theValue целое число для конвертации.
     * @return строковое представление целого числа.
     */
    public String toXMLLong(final Long theValue) {
        String result;
        if (theValue != null) {
            result = String.valueOf(theValue);
        } else {
            result = XmlConstants.EMPTY_INTEGER_AS_STRING;
        }
        return result;
    }

    /**
     * Возвращает Integer из строкового формата.
     *
     * @param theValue целое число для конвертации из строки.
     * @return целое число из строки.
     */
    public Integer fromXMLInteger(final String theValue) {
        Integer result;
        if ((theValue != null) && !theValue.equals(XmlConstants.EMPTY_INTEGER_AS_STRING)) {
            result = Integer.valueOf(theValue);
        } else {
            result = null;
        }
        return result;
    }

    /**
     * Возвращает Integer из строкового формата.
     *
     * @param theValue целое число для конвертации из строки.
     * @return целое число из строки.
     */
    public Long fromXMLLong(final String theValue) {
        Long result;
        if ((theValue != null) && !theValue.equals(XmlConstants.EMPTY_INTEGER_AS_STRING)) {
            result = Long.valueOf(theValue);
        } else {
            result = null;
        }
        return result;
    }

    /**
     * Возвращает Boolean в строковам формате.
     *
     * @param theValue boolean для конвертации.
     * @return строковое представление логической велиуины.
     */
    public String toXMLBoolean(final Boolean theValue) {
        String result;
        if (theValue != null) {
            if (theValue) {
                result = "true";
            } else {
                result = "false";
            }
        } else {
            result = XmlConstants.EMPTY_BOOLEAN_AS_STRING;
        }
        return result;
    }

    /**
     * Возвращает Boolean из строкового формата.
     *
     * @param theValue строка с boolean значением для конвертации.
     * @return логическая велиуина.
     */
    public Boolean fromXMLBoolean(final String theValue) {
        Boolean result;
        if ((theValue != null) && !theValue.equals(XmlConstants.EMPTY_BOOLEAN_AS_STRING)) {
            result = theValue.equals("true");
        } else {
            result = null;
        }
        return result;
    }

    /**
     * Выдате строку как строку, но с учетом соглашения о передачи NULL значений в
     * CommerceML.
     *
     * @param theValue сторока
     * @return откорректированная сторка.
     */
    public String toXMLString(final String theValue) {
        String result;
        if ((theValue != null) && (!theValue.equals(""))) {
            result = String.valueOf(theValue);
        } else {
            result = XmlConstants.EMPTY_STRING;
        }
        return result;
    }

    /**
     * Выдате строку как строку, но с учетом соглашения о передачи NULL значений в
     * CommerceML.
     *
     * @param theValue сторока
     * @return откорректированная сторка.
     */
    public String fromXMLString(final String theValue) {
        String result;
        if ((theValue != null) && (!theValue.equals(""))) {
            result = String.valueOf(theValue);
        } else {
            result = null;
        }
        return result;
    }

    /**
     * Выдает строковое значение для вывода в xml.
     *
     * @param theClass класс для конвертации.
     * @param theValue значение.
     * @return строка возврата.
     */
    protected String strValue(final Class theClass, final Object theValue) {
        if (theValue != null) {
            if (theClass.equals(Long.class)) {
                return toXMLLong((Long) theValue);
            }
            if (theClass.equals(Integer.class)) {
                return toXMLInteger((Integer) theValue);
            }
            if (theClass.equals(Float.class)) {
                return toXMLNumber((Double) theValue);
            }
            if (theClass.equals(Double.class)) {
                return toXMLDecimal((Double) theValue);
            }
            if (theClass.equals(String.class)) {
                return toXMLString((String) theValue);
            }
            if (theClass.equals(Calendar.class)) {
                return toXMLDateTime((Date) theValue);
            }
            if (theClass.equals(Date.class)) {
                return toXmlDate((Date) theValue);
            }
            if (theClass.equals(Time.class)) {
                return toXmlTime((Date) theValue);
            }
            if (theClass.equals(Boolean.class)) {
                return toXMLBoolean((Boolean) theValue);
            }
//            if (theClass.equals(Null.class)) {
//                return "";
//            }
            throw new RuntimeException("XmlConverter.strValue: Не определен "
                    + "тип класса для вывода в XML!");
        }
        return null;
    }

    /**
     * Выдает объектное значение для вывода в доменный объект из xml.
     *
     * @param theClass класс для конвертации.
     * @param theValue строка со значением в xml представлении.
     * @return объект возврата.
     * @throws java.text.ParseException ошибка парсера.
     */
    protected Object objValue(final Class theClass, final String theValue) throws ParseException {
        if (theValue != null) {
            if (theClass.equals(Long.class)) {
                return fromXMLLong(theValue);
            }
            if (theClass.equals(Integer.class)) {
                return fromXMLInteger(theValue);
            }
            if (theClass.equals(Float.class)) {
                return fromXMLNumber(theValue);
            }
            if (theClass.equals(Double.class)) {
                return fromXMLDecimal(theValue);
            }
            if (theClass.equals(String.class)) {
                return fromXMLString(theValue);
            }
            if (theClass.equals(Calendar.class)) {
                return fromXMLDateTime(theValue);
            }
            if (theClass.equals(Date.class)) {
                return fromXmlDate(theValue);
            }
            if (theClass.equals(Time.class)) {
                return fromXmlTime(theValue);
            }
            if (theClass.equals(Boolean.class)) {
                return fromXMLBoolean(theValue);
            }
            throw new RuntimeException("XmlConverter.strValue: Не определен "
                    + "тип класса для вывода в XML!");
        }
        return null;
    }

    /**
     * Врайтер.
     *
     * @return врайтер.
     */
    public HierarchicalStreamWriter getWriter() {
        return writer;
    }

    /**
     * Врайтер.
     *
     * @param theWriter врайтер.
     */
    public void setWriter(final HierarchicalStreamWriter theWriter) {
        writer = theWriter;
    }


    /**
     * Ридер.
     *
     * @return Ридер.
     */
    public HierarchicalStreamReader getReader() {
        return reader;
    }

    /**
     * Ридер.
     *
     * @param theReader Ридер.
     */
    public void setReader(final HierarchicalStreamReader theReader) {
        reader = theReader;
    }

    /**
     * Контекст врайтера.
     *
     * @return Контекст врайтера.
     */
    public MarshallingContext getMContext() {
        return mContext;
    }

    /**
     * Контекст врайтера.
     *
     * @param theMContext Контекст врайтера.
     */
    public void setMContext(final MarshallingContext theMContext) {
        mContext = theMContext;
    }

    /**
     * Контекст ридера.
     *
     * @return Контекст ридера.
     */
    public UnmarshallingContext getUContext() {
        return uContext;
    }

    /**
     * Контекст ридера.
     *
     * @param theUContext Контекст ридера.
     */
    public void setUContext(final UnmarshallingContext theUContext) {
        uContext = theUContext;
    }

    /**
     * Выводит в выходной XML пару имя/значение.
     *
     * @param theName       имя.
     * @param theValue      значение.
     * @param theClass      тип значения.
     * @param theAttributes атрибуты.
     */
    protected void writeNode(
            final String theName,
            final Object theValue,
            final Class theClass,
            final XmlAttribute... theAttributes) {
        getWriter().startNode(theName);
        String strValue;
        if (theAttributes != null) {
            for (XmlAttribute attribute : theAttributes) {
                strValue = strValue(attribute.getType(), attribute.getValue());
                if (strValue != null) {
                    getWriter().addAttribute(attribute.getName(), strValue);
                }
            }
        }
        strValue = strValue(theClass, theValue);
        if (strValue != null) {
            getWriter().setValue(strValue);
        }
        getWriter().endNode();
    }

    /**
     * Выводит в выходной XML пару имя/значение.
     *
     * @param theName       имя.
     * @param theValue      значение.
     * @param theClass      тип значения.
     * @param theAttributes атрибуты.
     */
    protected void writeNode1(
            final String theName,
            final Object theValue,
            final Class theClass,
            final XmlAttribute... theAttributes) {
        if (theValue != null) {
            getWriter().startNode(theName);
            String strValue;
            if (theAttributes != null) {
                for (XmlAttribute attribute : theAttributes) {
                    strValue = strValue(attribute.getType(), attribute.getValue());
                    if (strValue != null) {
                        getWriter().addAttribute(attribute.getName(), strValue);
                    }
                }
            }
            strValue = strValue(theClass, theValue);
            if (strValue != null) {
                getWriter().setValue(strValue);
            }
            getWriter().endNode();
        }
    }

    /**
     * Читает значение текущего элемента и его атрибутов из входного XML файла.
     *
     * @param theName       имя.
     * @param theClass      тип значения.
     * @param theAttributes атрибуты (заполняются только имена и тыпы).
     * @return значение текущего элемента xml.
     * @throws java.text.ParseException ошибка парсера.
     */
    protected Object readNode(
            final String theName,
            final Class theClass,
            final XmlAttribute... theAttributes) throws ParseException {
        Object result = null;
        if (readMoreFlag) {
            getReader().moveDown();
        }
        try {
            result = peekNode(theName, theClass, theAttributes);
        } finally {
            getReader().moveUp();
            readMoreFlag = true;
        }
        return result;
    }

    /**
     * Читает значение текущего элемента и его атрибутов из входного XML файла
     * без захода (заход должен бысть сделан ранее).
     *
     * @param theName       имя.
     * @param theClass      тип значения.
     * @param theAttributes атрибуты (заполняются только имена и тыпы).
     * @return значение текущего элемента xml.
     * @throws java.text.ParseException ошибка парсера.
     */
    protected Object peekNode(
            final String theName,
            final Class theClass,
            final XmlAttribute... theAttributes) throws ParseException {
        Object result;
        illegalXmlContentParse(theName);
        if (theAttributes != null) {
            for (XmlAttribute attribute : theAttributes) {
                attribute.setValue(objValue(
                        attribute.getType(), getReader().getAttribute(attribute.getName())));
            }
        }
        result = objValue(theClass, getReader().getValue());
        return result;
    }

    /**
     * Генерим ошибку несоответствия ожидаемого имени элемента с действительным.
     *
     * @param theName ожидаемое имя элемента.
     */
    protected void illegalXmlContent(final String theName) {
        if (!getReader().getNodeName().equals(theName)) {
            throw new RuntimeException("Ожидаемое имя элемента '" + theName
                    + "', актуальное, однако '" + getReader().getNodeName() + "'!");
        }
    }

    /**
     * Генерим ошибку несоответствия ожидаемого имени элемента с действительным.
     *
     * @param theName ожидаемое имя элемента.
     * @throws java.text.ParseException ошибка парсера.
     */
    protected void illegalXmlContentParse(final String theName) throws ParseException {
        if (!getReader().getNodeName().equals(theName)) {
            throw new ParseException("Ожидаемое имя элемента '" + theName
                    + "', актуальное, однако '" + getReader().getNodeName() + "'!", 0);
        }
    }

    /**
     * Проверяет имя следующего элемента.
     *
     * @param theName имя следующего элемента.
     * @return соответствие имен.
     */
    protected Boolean checkElementName(final String theName) {
        Boolean result;
        if (readMoreFlag) {
            getReader().moveDown();
            readMoreFlag = false;
        }
        result = getReader().getNodeName().equals(theName);
        return result;
    }

    /**
     * Проверяет имя следующего элемента.
     *
     * @return Имя слудующего элемента.
     */
    protected String getNextElement() {
        String result = null;
        if (getReader().hasMoreChildren()) {
            getReader().moveDown();
            result = getReader().getNodeName();
        }
        return result;
    }

    /**
     * Флаг, cчитывать ли следующий элемент из иерархического ридера.
     *
     * @return Флаг, cчитывать ли следующий элемент из иерархического ридера.
     */
    public Boolean getReadMoreFlag() {
        return readMoreFlag;
    }

    /**
     * Флаг, cчитывать ли следующий элемент из иерархического ридера.
     *
     * @param theReadMoreFlag Флаг, cчитывать ли следующий элемент из иерархического ридера.
     */
    public void setReadMoreFlag(final Boolean theReadMoreFlag) {
        if (!readMoreFlag) {
            if (theReadMoreFlag) {
                getReader().moveUp();
                readMoreFlag = true;
            }
        }
    }

    /**
     * Кодирование в Base64.
     *
     * @param data данные
     * @param log  логгер.
     * @return данные в формате Base64
     */
    protected String encodeBase64(final byte[] data, final Log log) {
        String result = null;
        if (data != null) {
            byte[] encoded = Base64.encodeBase64(data);
            try {
                result = new String(encoded, CHARSET_NAME);
            } catch (UnsupportedEncodingException e) {
                log.error(BASE64_ERROR, e);
            }
        }
        return result;

    }

    /**
     * Перекодирует BMP в JPEG.
     *
     * @param bmpImage изображение в формате BMP
     * @param log      логгер.
     * @return изображение в формате JPEG
     */
    protected static byte[] toJpeg(final byte[] bmpImage, final Log log) {
        byte[] jpeg = null;
        try {
            ByteArrayInputStream is = new ByteArrayInputStream(bmpImage);
            BufferedImage image = ImageIO.read(is);
            if (image != null) {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(image, "jpeg", os);
                jpeg = os.toByteArray();
                IOUtils.closeQuietly(os);
            }
            IOUtils.closeQuietly(is);
        } catch (IOException e) {
            log.error(JPEG_ENCODE_ERROR, e);
        }

        return jpeg;
    }

    /**
     * Перекодирует JPEG в BMP.
     *
     * @param jpegImage изображение в формате JPEG
     * @param log       логгер.
     * @return изображение в формате BMP
     */
    protected static byte[] toBmp(final byte[] jpegImage, final Log log) {
        byte[] bmp = null;
        try {
            ByteArrayInputStream is = new ByteArrayInputStream(jpegImage);
            BufferedImage image = ImageIO.read(is);
            if (image != null) {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(image, "bmp", os);
                bmp = os.toByteArray();
                IOUtils.closeQuietly(os);
            } else {
                log.error(BMP_ENCODE_ERROR);
            }
            IOUtils.closeQuietly(is);
        } catch (IOException e) {
            log.error(BMP_ENCODE_ERROR, e);
        }

        return bmp;
    }

    /**
     * Декодирование из Base64.
     *
     * @param data данные в формате Base64
     * @param log  логгер.
     * @return декодированные данные
     */
    protected byte[] decodeBase64(final String data, final Log log) {
        byte[] decoded = null;
        if (data != null) {
            try {
                decoded = Base64.decodeBase64(data.getBytes(CHARSET_NAME));
            } catch (UnsupportedEncodingException e) {
                log.error(BASE64_ERROR, e);
            }
        }
        return decoded;
    }

}
