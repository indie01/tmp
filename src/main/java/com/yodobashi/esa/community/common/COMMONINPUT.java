
package com.yodobashi.esa.community.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.kickmogu.lib.core.domain.CommonInputIF;


/**
 * <p>Java class for COMMON_INPUT complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="COMMON_INPUT">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="externalSystem" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="objectKey" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="terminalID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="traceID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "COMMON_INPUT", propOrder = {
    "externalSystem",
    "objectKey",
    "terminalID",
    "traceID"
})
public class COMMONINPUT implements CommonInputIF{

    /**
	 * 
	 */
	private static final long serialVersionUID = 2272645276808494635L;
	
	protected String externalSystem;
    protected String objectKey;
    protected String terminalID;
    protected String traceID;

    /**
     * Gets the value of the externalSystem property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExternalSystem() {
        return externalSystem;
    }

    /**
     * Sets the value of the externalSystem property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExternalSystem(String value) {
        this.externalSystem = value;
    }

    /**
     * Gets the value of the objectKey property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getObjectKey() {
        return objectKey;
    }

    /**
     * Sets the value of the objectKey property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setObjectKey(String value) {
        this.objectKey = value;
    }

    /**
     * Gets the value of the terminalID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTerminalID() {
        return terminalID;
    }

    /**
     * Sets the value of the terminalID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTerminalID(String value) {
        this.terminalID = value;
    }

    /**
     * Gets the value of the traceID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTraceID() {
        return traceID;
    }

    /**
     * Sets the value of the traceID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTraceID(String value) {
        this.traceID = value;
    }

}
