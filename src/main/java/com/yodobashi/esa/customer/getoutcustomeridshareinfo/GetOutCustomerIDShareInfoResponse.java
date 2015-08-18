
package com.yodobashi.esa.customer.getoutcustomeridshareinfo;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import com.yodobashi.esa.customer.structure.COMMONRETURN;


/**
 * <p>Java class for GetOutCustomerIDShareInfo_Response complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetOutCustomerIDShareInfo_Response">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="shareInfoList" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="outerCustomerId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="outerCustomerIdShareInfoList" maxOccurs="unbounded" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="outerCustomerId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                             &lt;element name="outerCustomerStatus" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                             &lt;element name="customerType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="COMMON_RETURN" type="{http://esa.yodobashi.com/Customer/Structure}COMMON_RETURN" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetOutCustomerIDShareInfo_Response", propOrder = {
    "shareInfoList",
    "commonreturn"
})
public class GetOutCustomerIDShareInfoResponse {

    protected List<GetOutCustomerIDShareInfoResponse.ShareInfoList> shareInfoList;
    @XmlElement(name = "COMMON_RETURN")
    protected COMMONRETURN commonreturn;

    /**
     * Gets the value of the shareInfoList property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the shareInfoList property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getShareInfoList().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GetOutCustomerIDShareInfoResponse.ShareInfoList }
     * 
     * 
     */
    public List<GetOutCustomerIDShareInfoResponse.ShareInfoList> getShareInfoList() {
        if (shareInfoList == null) {
            shareInfoList = new ArrayList<GetOutCustomerIDShareInfoResponse.ShareInfoList>();
        }
        return this.shareInfoList;
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
     *         &lt;element name="outerCustomerIdShareInfoList" maxOccurs="unbounded" minOccurs="0">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;sequence>
     *                   &lt;element name="outerCustomerId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *                   &lt;element name="outerCustomerStatus" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *                   &lt;element name="customerType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *                 &lt;/sequence>
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
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
        "outerCustomerIdShareInfoList"
    })
    public static class ShareInfoList {

        protected String outerCustomerId;
        protected List<GetOutCustomerIDShareInfoResponse.ShareInfoList.OuterCustomerIdShareInfoList> outerCustomerIdShareInfoList;

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
         * Gets the value of the outerCustomerIdShareInfoList property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the outerCustomerIdShareInfoList property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getOuterCustomerIdShareInfoList().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link GetOutCustomerIDShareInfoResponse.ShareInfoList.OuterCustomerIdShareInfoList }
         * 
         * 
         */
        public List<GetOutCustomerIDShareInfoResponse.ShareInfoList.OuterCustomerIdShareInfoList> getOuterCustomerIdShareInfoList() {
            if (outerCustomerIdShareInfoList == null) {
                outerCustomerIdShareInfoList = new ArrayList<GetOutCustomerIDShareInfoResponse.ShareInfoList.OuterCustomerIdShareInfoList>();
            }
            return this.outerCustomerIdShareInfoList;
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
         *         &lt;element name="outerCustomerStatus" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
         *         &lt;element name="customerType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
            "outerCustomerStatus",
            "customerType"
        })
        public static class OuterCustomerIdShareInfoList {

            protected String outerCustomerId;
            protected String outerCustomerStatus;
            protected String customerType;

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
             * Gets the value of the customerType property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getCustomerType() {
                return customerType;
            }

            /**
             * Sets the value of the customerType property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setCustomerType(String value) {
                this.customerType = value;
            }

        }

    }

}
