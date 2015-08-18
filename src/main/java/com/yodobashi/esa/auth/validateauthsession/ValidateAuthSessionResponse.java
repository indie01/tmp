
package com.yodobashi.esa.auth.validateauthsession;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.yodobashi.esa.auth.type.AuthReturn;
import com.yodobashi.esa.common.COMMONRETURN;


/**
 * <p>Java class for ValidateAuthSession_Response complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ValidateAuthSession_Response">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="AuthReturn" type="{http://esa.yodobashi.com/AUTH/type}AuthReturn" minOccurs="0"/>
 *         &lt;element name="COMMON_RETURN" type="{http://esa.yodobashi.com/common}COMMON_RETURN" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ValidateAuthSession_Response", propOrder = {
    "authReturn",
    "commonreturn"
})
public class ValidateAuthSessionResponse {

    @XmlElement(name = "AuthReturn")
    protected AuthReturn authReturn;
    @XmlElement(name = "COMMON_RETURN")
    protected COMMONRETURN commonreturn;

    /**
     * Gets the value of the authReturn property.
     * 
     * @return
     *     possible object is
     *     {@link AuthReturn }
     *     
     */
    public AuthReturn getAuthReturn() {
        return authReturn;
    }

    /**
     * Sets the value of the authReturn property.
     * 
     * @param value
     *     allowed object is
     *     {@link AuthReturn }
     *     
     */
    public void setAuthReturn(AuthReturn value) {
        this.authReturn = value;
    }

    /**
     * Gets the value of the commonreturn property.
     * 
     * @return
     *     possible object is
     *     {@link COMMONRETURN }
     *     
     */
    public COMMONRETURN getCOMMONRETURN() {
        return commonreturn;
    }

    /**
     * Sets the value of the commonreturn property.
     * 
     * @param value
     *     allowed object is
     *     {@link COMMONRETURN }
     *     
     */
    public void setCOMMONRETURN(COMMONRETURN value) {
        this.commonreturn = value;
    }

}
