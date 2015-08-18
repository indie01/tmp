
package com.yodobashi.esa.customer.getcustomerid;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.kickmogu.lib.core.resource.domain.BaseDO;
import com.yodobashi.esa.customer.structure.COMMONRETURN;


/**
 * <p>Java class for GetCustomerID_Response complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="GetCustomerID_Response">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="outerCustomerList" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="outerCustomerId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="outerCustomerType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="outerCustomerStatus" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="custNo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="COMMON_Return" type="{http://esa.yodobashi.com/Customer/Structure}COMMON_RETURN" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetCustomerID_Response", propOrder = {
    "outerCustomerList",
    "commonReturn"
})
public class GetCustomerIDResponse {

    protected List<GetCustomerIDResponse.OuterCustomerList> outerCustomerList;
    @XmlElement(name = "COMMON_Return")
    protected COMMONRETURN commonReturn;

    /**
     * Gets the value of the outerCustomerList property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the outerCustomerList property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOuterCustomerList().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GetCustomerIDResponse.OuterCustomerList }
     *
     *
     */
    public List<GetCustomerIDResponse.OuterCustomerList> getOuterCustomerList() {
        if (outerCustomerList == null) {
            outerCustomerList = new ArrayList<GetCustomerIDResponse.OuterCustomerList>();
        }
        return this.outerCustomerList;
    }

    /**
     * Gets the value of the commonReturn property.
     *
     * @return
     *     possible object is
     *     {@link COMMONRETURN }
     *
     */
    public COMMONRETURN getCOMMONReturn() {
        return commonReturn;
    }

    /**
     * Sets the value of the commonReturn property.
     *
     * @param value
     *     allowed object is
     *     {@link COMMONRETURN }
     *
     */
    public void setCOMMONReturn(COMMONRETURN value) {
        this.commonReturn = value;
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
     *         &lt;element name="outerCustomerStatus" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="custNo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
        "outerCustomerType",
        "outerCustomerStatus",
        "custNo"
    })
    public static class OuterCustomerList extends BaseDO {

        /**
		 *
		 */
		private static final long serialVersionUID = -138718615098879717L;

		protected String outerCustomerId;
        protected String outerCustomerType;
        protected String outerCustomerStatus;
        protected String custNo;

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

        /**
         * Gets the value of the outerCustomerStatus property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getOuterCustomerStatus() {
            return outerCustomerStatus;
        }

        /**
         * Sets the value of the outerCustomerStatus property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setOuterCustomerStatus(String value) {
            this.outerCustomerStatus = value;
        }

        /**
         * Gets the value of the custNo property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getCustNo() {
            return custNo;
        }

        /**
         * Sets the value of the custNo property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setCustNo(String value) {
            this.custNo = value;
        }

    }

}
