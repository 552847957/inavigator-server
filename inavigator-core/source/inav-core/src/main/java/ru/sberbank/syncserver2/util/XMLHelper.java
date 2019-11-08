package ru.sberbank.syncserver2.util;

import java.io.*;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * Created by IntelliJ IDEA.
 * User: sbt-kozhinskiy-lb
 * Date: 25.01.12
 * Time: 17:04
 * To change this template use File | Settings | File Templates.
 */
public class XMLHelper {
    private static ThreadLocal bufferHolder = new ThreadLocal();
    private static ConcurrentHashMap jaxbContexts = new ConcurrentHashMap();

    private static Buffer borrowBuffer(){
        Buffer buffer = (Buffer) bufferHolder.get();
        if(buffer==null){
            buffer = new Buffer();
            bufferHolder.set(buffer);
        }
        return buffer;
    }

    private static void freeBuffer(Buffer buffer){
        buffer.reset();
    }


    public static class Buffer extends ByteArrayOutputStream {
        public Buffer() {
            super(16384);
        }
    }


    public static void writeXML(OutputStream out, Object data, boolean spacing, Class... classes) throws JAXBException {
        Class c = data.getClass();
        ClassLoader cl = c.getClassLoader();
        Thread.currentThread().setContextClassLoader(cl);
        JAXBContext context = getJAXBContext(classes);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_ENCODING,"UTF-8");
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, new Boolean(spacing));
        marshaller.marshal(data, out);
    }

    public static void writeXML(String outputFileName, Object data, boolean spacing, Class... classes){
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(outputFileName);
            writeXML(out, data, spacing, classes);
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (JAXBException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }


    public static String writeXMLToString(Object data, boolean spacing, Class... classes){
        Buffer buffer = null;
        try {
            buffer = borrowBuffer();
            buffer.reset();
            writeXML(buffer, data, spacing, classes);
            return buffer.toString("UTF-8");
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return null;
        } finally {
            freeBuffer(buffer);
        }
    }

    public static void writeXMLWithBuffer(OutputStream outputStream, Object data, boolean spacing,Class... classes){
    	writeXMLWithBuffer(outputStream, data, spacing,null,classes);
    }
        
    public static void writeXMLWithBuffer(OutputStream outputStream, Object data, boolean spacing,HttpServletResponse response, Class... classes){
        Buffer buffer = null;
        try {
            buffer = borrowBuffer();
            buffer.reset();
            writeXML(buffer, data, spacing, classes);
            // если задан HttpServletResponse, то размер ответа записываем в заголовок HTTP ответа
            if (response != null)
            	response.setContentLength(buffer.size());
            buffer.writeTo(outputStream);
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            freeBuffer(buffer);
        }
    }

    public static Object readXML(InputStream is, Class... classes) throws JAXBException {
        ClassLoader cl = classes[0].getClassLoader();
        Thread.currentThread().setContextClassLoader(cl);
        JAXBContext context = getJAXBContext(classes);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        Object report = unmarshaller.unmarshal(is);
        return report;
    }

    public static Object readXMLFromString(String inputXmlAsString, Class... classes) throws JAXBException, UnsupportedEncodingException {
        return readXMLFromByteArray(inputXmlAsString.getBytes("UTF8"), classes);
    }

    public static Object readXMLFromByteArray(byte[] inputXmlAsByteArray, Class... classes) throws JAXBException, UnsupportedEncodingException {
        ClassLoader cl = classes[0].getClassLoader();
        Thread.currentThread().setContextClassLoader(cl);
        JAXBContext context = getJAXBContext(classes);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        InputStream is = new ByteArrayInputStream(inputXmlAsByteArray);
        Object report = unmarshaller.unmarshal(is);
        return report;
    }


    public static Object readXML(String inputFileName, Class... c){
        return readXML(new File(inputFileName), c);
    }


    public static Object readXML(File inputFile, Class... c){
        FileInputStream is = null;
        try {
            is = new FileInputStream(inputFile);
            return readXML(is, c);
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (JAXBException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        return null;
    }

    private static class ClasssesKey {
        private Class[] classes;

        private ClasssesKey(Class[] classes) {
            this.classes = classes;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ClasssesKey that = (ClasssesKey) o;

            if (!Arrays.equals(classes, that.classes)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return classes != null ? Arrays.hashCode(classes) : 0;
        }
    }

    private static JAXBContext getJAXBContext(Class[] classes){
        ClasssesKey key = new ClasssesKey(classes);
        JAXBContext jaxbContext = (JAXBContext) jaxbContexts.get(key);
        if (jaxbContext == null) {
            try {
                jaxbContext = JAXBContext.newInstance(classes);
                jaxbContexts.putIfAbsent(key, jaxbContext);
            } catch (JAXBException ex) {
                throw new RuntimeException(
                        "Could not instantiate JAXBContext for classes [" + classes + "]: " + ex.getMessage(), ex);
            }
        }
        return jaxbContext;
    }
}
