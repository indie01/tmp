
package com.yodobashi.esa.usm.type;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for UniversalSessionReturn complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="UniversalSessionReturn">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="universalSessionID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="realm" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="exclusiveID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="expires" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="disappears" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="status" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="nvPair" type="{http://esa.yodobashi.com/USM/type}nvPair" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="statusUpdateDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="registDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="updateDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
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
@XmlType(name = "UniversalSessionReturn", propOrder = {
    "universalSessionID",
    "realm",
    "exclusiveID",
    "expires",
    "disappears",
    "status",
    "nvPair",
    "statusUpdateDate",
    "registDate",
    "updateDate",
    "extension"
})
public class UniversalSessionReturn {

    protected String universalSessionID;
    protected String realm;
    protected String exclusiveID;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar expires;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar disappears;
    protected String status;
    protected List<NvPair> nvPair;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar statusUpdateDate;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar registDate;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar updateDate;
    protected String extension;

    /**
     * Gets the value of the universalSessionID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUniversalSessionID() {
        return universalSessionID;
    }

    /**
     * Sets the value of the universalSessionID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUniversalSessionID(String value) {
        this.universalSessionID = value;
    }

    /**
     * Gets the value of the realm property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRealm() {
        return realm;
    }

    /**
     * Sets the value of the realm property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRealm(String value) {
        this.realm = value;
    }

    /**
     * Gets the value of the exclusiveID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExclusiveID() {
        return exclusiveID;
    }

    /**
     * Sets the value of the exclusiveID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExclusiveID(String value) {
        this.exclusiveID = value;
    }

    /**
     * Gets the value of the expires property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getExpires() {
        return expires;
    }

    /**
     * Sets the value of the expires property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setExpires(XMLGregorianCalendar value) {
        this.expires = value;
    }

    /**
     * Gets the value of the disappears property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDisappears() {
        return disappears;
    }

    /**
     * Sets the value of the disappears property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDisappears(XMLGregorianCalendar value) {
        this.disappears = value;
    }

    /**
     * Gets the value of the status property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the value of the status property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStatus(String value) {
        this.status = value;
    }

    /**
     * Gets the value of the nvPair property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the nvPair property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNvPair().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link NvPair }
     * 
     * 
     */
    public List<NvPair> getNvPair() {
        if (nvPair == null) {
            nvPair = new ArrayList<NvPair>();
        }
        return this.nvPair;
    }

    /**
     * Gets the value of the statusUpdateDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getStatusUpdateDate() {
        return statusUpdateDate;
    }

    /**
     * Sets the value of the statusUpdateDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setStatusUpdateDate(XMLGregorianCalendar value) {
        this.statusUpdateDate = value;
    }

    /**
     * Gets the value of the registDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getRegistDate() {
        return registDate;
    }

    /**
     * Sets the value of the registDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setRegistDate(XMLGregorianCalendar value) {
        this.registDate = value;
    }

    /**
     * Gets the value of the updateDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getUpdateDate() {
        return updateDate;
    }

    /**
     * Sets the value of the updateDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setUpdateDate(XMLGregorianCalendar value) {
        this.updateDate = value;
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
