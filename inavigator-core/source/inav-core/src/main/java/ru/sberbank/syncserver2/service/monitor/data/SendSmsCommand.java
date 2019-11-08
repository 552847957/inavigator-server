package ru.sberbank.syncserver2.service.monitor.data;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.annotation.*;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: SBT-Karmanov-AV
 * Date: 22.03.13
 * Time: 16:05
 */
@XmlRootElement(name = "send-sms")
@XmlAccessorType(XmlAccessType.FIELD)
public class SendSmsCommand {
    private Target target;

    private String text;

    @XmlElementWrapper(name = "addresses")
    @XmlElement(name = "address")
    private List<String> address = new ArrayList<String>();

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<String> getAddress() {
        return address;
    }

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }

    public void setAddress(List<String> address) {
        this.address = address;
    }

    public static void main(String[] args) throws JAXBException, IOException {
        SendSmsCommand jaxbElement = new SendSmsCommand();
        jaxbElement.setTarget(Target.sms);
        jaxbElement.setText("123");
        jaxbElement.setAddress(Arrays.asList("123", "123"));


        JAXBContext context = JAXBContext.newInstance(SendSmsCommand.class);
        context.generateSchema(new SchemaOutputResolver() {
            @Override
            public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
                return new StreamResult(new File(suggestedFileName));
            }
        });
        Marshaller marshaller = context.createMarshaller();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        marshaller.marshal(jaxbElement, output);

        System.out.println(new String(output.toByteArray()));
    }

}
