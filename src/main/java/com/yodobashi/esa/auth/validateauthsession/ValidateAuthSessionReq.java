
package com.yodobashi.esa.auth.validateauthsession;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.yodobashi.esa.common.COMMONINPUT;


/**
 * <p>Java class for ValidateAuthSession_Req complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ValidateAuthSession_Req">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="authSessionID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="newTimeout" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="COMMON_INPUT" type="{http://esa.yodobashi.com/common}COMMON_INPUT" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ValidateAuthSession_Req", propOrder = {
    "authSessionID",
    "newTimeout",
    "commoninput"
})
public class ValidateAuthSessionReq {

    protected String authSessionID;
    protected Integer newTimeout;
    @XmlElement(name = "COMMON_INPUT")
    protected COMMONINPUT commoninput;

    /**
     * Gets the value of the authSessionID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAuthSessionID() {
        return authSessionID;
    }

    /**
     * Sets the value of the authSessionID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAuthSessionID(String value) {
        this.authSessionID = value;
    }

    /**
     * Gets the value of the newTimeout property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getNewTimeout() {
        return newTimeout;
    }

    /**
     * Sets the value of the newTimeout property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setNewTimeout(Integer value) {
        this.newTimeout = value;
    }

    /**
     * Gets the value of the commoninput property.
     * 
     * @return
     *     possible object is
     *     {@link COMMONINPUT }
     *     
     */
    public COMMONINPUT getCOMMONINPUT() {
        return commoninput;
    }

    /**
     * Sets the value of the commoninput property.
     * 
     * @param value
     *     allowed object is
     *     {@link COMMONINPUT }
     *     
     */
    public void setCOMMONINPUT(COMMONINPUT value) {
        this.commoninput = value;
    }

}
