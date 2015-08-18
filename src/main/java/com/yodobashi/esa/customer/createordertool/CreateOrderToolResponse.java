
package com.yodobashi.esa.customer.createordertool;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.yodobashi.esa.customer.structure.COMMONRETURN;


/**
 * <p>Java class for CreateOrderTool_Response complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CreateOrderTool_Response">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="COMMON_RETURN" type="{http://esa.yodobashi.com/Customer/Structure}COMMON_RETURN" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CreateOrderTool_Response", propOrder = {
    "commonreturn"
})
public class CreateOrderToolResponse {

    @XmlElement(name = "COMMON_RETURN")
    protected COMMONRETURN commonreturn;

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
