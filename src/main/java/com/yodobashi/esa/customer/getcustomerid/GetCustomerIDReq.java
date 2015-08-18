
package com.yodobashi.esa.customer.getcustomerid;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.yodobashi.esa.customer.structure.COMMONINPUT;


/**
 * <p>Java class for GetCustomerID_Req complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetCustomerID_Req">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="customerList" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="outerCustomerId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="outerCustomerType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="COMMON_INPUT" type="{http://esa.yodobashi.com/Customer/Structure}COMMON_INPUT" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetCustomerID_Req", propOrder = {
    "customerList",
    "commoninput"
})
public class GetCustomerIDReq {

    protected List<GetCustomerIDReq.CustomerList> customerList;
    @XmlElement(name = "COMMON_INPUT")
    protected COMMONINPUT commoninput;

    /**
     * Gets the value of the customerList property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the customerList property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCustomerList().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GetCustomerIDReq.CustomerList }
     * 
     * 
     */
    public List<GetCustomerIDReq.CustomerList> getCustomerList() {
        if (customerList == null) {
            customerList = new ArrayList<GetCustomerIDReq.CustomerList>();
        }
        return this.customerList;
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


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="outerCustomerId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="outerCustomerType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "outerCustomerId",
        "outerCustomerType"
    })
    public static class CustomerList {

        protected String outerCustomerId;
        protected String outerCustomerType;

        /**
         * Gets the value of the outerCustomerId property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getOuterCustomerId() {
            return outerCustomerId;
        }

        /**
         * Sets the value of the outerCustomerId property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setOuterCustomerId(String value) {
            this.outerCustomerId = value;
        }

        /**
         * Gets the value of the outerCustomerType property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getOuterCustomerType() {
            return outerCustomerType;
        }

        /**
         * Sets the value of the outerCustomerType property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setOuterCustomerType(String value) {
            this.outerCustomerType = value;
        }

    }

}
