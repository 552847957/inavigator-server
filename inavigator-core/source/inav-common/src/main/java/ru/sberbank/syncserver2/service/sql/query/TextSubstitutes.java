package ru.sberbank.syncserver2.service.sql.query;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "textSubstitute"
})
public class TextSubstitutes {

    @XmlElement(name = "TextSubstitute", required = true)
    protected List<TextSubstitute> textSubstitute;

    public List<TextSubstitute> getTextSubstitute() {
        if (textSubstitute == null) {
            textSubstitute = new ArrayList<TextSubstitute>();
        }
        return this.textSubstitute;
    }
}
