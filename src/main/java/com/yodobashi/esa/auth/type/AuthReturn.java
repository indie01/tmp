
package com.yodobashi.esa.auth.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for AuthReturn complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AuthReturn">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="customerCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="customerKeyID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="customerKeyIDType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="authKey" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="authSessionID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="authLevel" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="authMethod" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="validity" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="authSessionIDUpdated" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="extension" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AuthReturn", propOrder = {
    "customerCode",
    "customerKeyID",
    "customerKeyIDType",
    "authKey",
    "authSessionID",
    "authLevel",
    "authMethod",
    "validity",
    "authSessionIDUpdated",
    "extension"
})
public class AuthReturn {

    protected String customerCode;
    protected String customerKeyID;
    protected String customerKeyIDType;
    protected String authKey;
    protected String authSessionID;
    protected Integer authLevel;
    protected String authMethod;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar validity;
    protected Boolean authSessionIDUpdated;
    protected String extension;

    /**
     * Gets the value of the customerCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCustomerCode() {
        return customerCode;
    }

    /**
     * Sets the value of the customerCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCustomerCode(String value) {
        this.customerCode = value;
    }

    /**
     * Gets the value of the customerKeyID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCustomerKeyID() {
        return customerKeyID;
    }

    /**
     * Sets the value of the customerKeyID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCustomerKeyID(String value) {
        this.customerKeyID = value;
    }

    /**
     * Gets the value of the customerKeyIDType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCustomerKeyIDType() {
        return customerKeyIDType;
    }

    /**
     * Sets the value of the customerKeyIDType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCustomerKeyIDType(String value) {
        this.customerKeyIDType = value;
    }

    /**
     * Gets the value of the authKey property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAuthKey() {
        return authKey;
    }

    /**
     * Sets the value of the authKey property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAuthKey(String value) {
        this.authKey = value;
    }

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
     * Gets the value of the authLevel property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getAuthLevel() {
        return authLevel;
    }

    /**
     * Sets the value of the authLevel property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setAuthLevel(Integer value) {
        this.authLevel = value;
    }

    /**
     * Gets the value of the authMethod property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAuthMethod() {
        return authMethod;
    }

    /**
     * Sets the value of the authMethod property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAuthMethod(String value) {
        this.authMethod = value;
    }

    /**
     * Gets the value of the validity property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getValidity() {
        return validity;
    }

    /**
     * Sets the value of the validity property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setValidity(XMLGregorianCalendar value) {
        this.validity = value;
    }

    /**
     * Gets the value of the authSessionIDUpdated property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isAuthSessionIDUpdated() {
        return authSessionIDUpdated;
    }

    /**
     * Sets the value of the authSessionIDUpdated property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAuthSessionIDUpdated(Boolean value) {
        this.authSessionIDUpdated = value;
    }

    /**
     * Gets the value of the extension property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExtension() {
        return extension;
    }

    /**
     * Sets the value of the extension property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExtension(String value) {
        this.extension = value;
    }

}
