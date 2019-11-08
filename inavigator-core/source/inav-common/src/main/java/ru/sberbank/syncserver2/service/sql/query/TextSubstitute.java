package ru.sberbank.syncserver2.service.sql.query;

import javax.xml.bind.annotation.*;

@XmlRootElement(name = "TextSubstitute")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
public class TextSubstitute {

    @XmlAttribute(name = "substituteVarName", required = true)
    protected String substituteVarName;
    @XmlAttribute(name = "substituteCode", required = true)
    protected String substituteCode;

    /**
     * Gets the value of the substituteVarName property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getSubstituteVarName() {
        return substituteVarName;
    }

    /**
     * Sets the value of the substituteVarName property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setSubstituteVarName(String value) {
        this.substituteVarName = value;
    }

    /**
     * Gets the value of the substituteCode property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getSubstituteCode() {
        return substituteCode;
    }

    /**
     * Sets the value of the substituteCode property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setSubstituteCode(String value) {
        this.substituteCode = value;
    }

}

