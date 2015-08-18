
package com.yodobashi.esa.usm.searchuniversalsession;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.yodobashi.esa.common.COMMONRETURN;
import com.yodobashi.esa.usm.type.UniversalSessionReturn;


/**
 * <p>Java class for SearchUniversalSession_Response complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SearchUniversalSession_Response">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="UniversalSessionReturn" type="{http://esa.yodobashi.com/USM/type}UniversalSessionReturn" minOccurs="0"/>
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
@XmlType(name = "SearchUniversalSession_Response", propOrder = {
    "universalSessionReturn",
    "commonreturn"
})
public class SearchUniversalSessionResponse {

    @XmlElement(name = "UniversalSessionReturn")
    protected UniversalSessionReturn universalSessionReturn;
    @XmlElement(name = "COMMON_RETURN")
    protected COMMONRETURN commonreturn;

    /**
     * Gets the value of the universalSessionReturn property.
     * 
     * @return
     *     possible object is
     *     {@link UniversalSessionReturn }
     *     
     */
    public UniversalSessionReturn getUniversalSessionReturn() {
        return universalSessionReturn;
    }

    /**
     * Sets the value of the universalSessionReturn property.
     * 
     * @param value
     *     allowed object is
     *     {@link UniversalSessionReturn }
     *     
     */
    public void setUniversalSessionReturn(UniversalSessionReturn value) {
        this.universalSessionReturn = value;
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
