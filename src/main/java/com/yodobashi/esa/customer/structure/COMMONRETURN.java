
package com.yodobashi.esa.customer.structure;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.kickmogu.lib.core.domain.CommonReturnIF;
import com.kickmogu.lib.core.domain.ReturnComDetailIF;
import com.kickmogu.lib.core.domain.ReturnComHeaderIF;


/**
 * <p>Java class for COMMON_RETURN complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="COMMON_RETURN">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ReturnComHeader" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="statusFlg" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *                   &lt;element name="statusCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="messageText" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="param_1" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="param_2" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="ReturnComDetail" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="detailStatus" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="messageText" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="param_1" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="param_2" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="param_3" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="param_4" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="param_5" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
@XmlType(name = "COMMON_RETURN", propOrder = {
    "returnComHeader",
    "returnComDetail"
})
public class COMMONRETURN implements CommonReturnIF {

    /**
	 *
	 */
	private static final long serialVersionUID = -8379482943749083077L;

	@XmlElement(name = "ReturnComHeader")
    protected COMMONRETURN.ReturnComHeader returnComHeader;
    @XmlElement(name = "ReturnComDetail")
    protected List<COMMONRETURN.ReturnComDetail> returnComDetail;

	public static final COMMONRETURN SUCCESS;

	static {
		SUCCESS = new COMMONRETURN();
		SUCCESS.returnComHeader = new ReturnComHeader();
		SUCCESS.returnComHeader.statusFlg = true;
		SUCCESS.returnComHeader.statusCode = "000";
	}

    public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    /**
     * Gets the value of the returnComHeader property.
     *
     * @return
     *     possible object is
     *     {@link COMMONRETURN.ReturnComHeader }
     *
     */
    public COMMONRETURN.ReturnComHeader getReturnComHeader() {
        return returnComHeader;
    }

    /**
     * Sets the value of the returnComHeader property.
     *
     * @param value
     *     allowed object is
     *     {@link COMMONRETURN.ReturnComHeader }
     *
     */
    public void setReturnComHeader(COMMONRETURN.ReturnComHeader value) {
        this.returnComHeader = value;
    }

    /**
     * Gets the value of the returnComDetail property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the returnComDetail property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getReturnComDetail().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link COMMONRETURN.ReturnComDetail }
     *
     *
     */
    public List<COMMONRETURN.ReturnComDetail> getReturnComDetail() {
        if (returnComDetail == null) {
            returnComDetail = new ArrayList<COMMONRETURN.ReturnComDetail>();
        }
        return this.returnComDetail;
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
     *         &lt;element name="detailStatus" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="messageText" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="param_1" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="param_2" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="param_3" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="param_4" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="param_5" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
        "detailStatus",
        "messageText",
        "param1",
        "param2",
        "param3",
        "param4",
        "param5"
    })
    public static class ReturnComDetail implements ReturnComDetailIF {

        /**
		 *
		 */
		private static final long serialVersionUID = 2994240977432892616L;

		protected String detailStatus;
        protected String messageText;
        @XmlElement(name = "param_1")
        protected String param1;
        @XmlElement(name = "param_2")
        protected String param2;
        @XmlElement(name = "param_3")
        protected String param3;
        @XmlElement(name = "param_4")
        protected String param4;
        @XmlElement(name = "param_5")
        protected String param5;

        public String toString() {
    		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
        }

        /**
         * Gets the value of the detailStatus property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getDetailStatus() {
            return detailStatus;
        }

        /**
         * Sets the value of the detailStatus property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setDetailStatus(String value) {
            this.detailStatus = value;
        }

        /**
         * Gets the value of the messageText property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getMessageText() {
            return messageText;
        }

        /**
         * Sets the value of the messageText property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setMessageText(String value) {
            this.messageText = value;
        }

        /**
         * Gets the value of the param1 property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getParam1() {
            return param1;
        }

        /**
         * Sets the value of the param1 property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setParam1(String value) {
            this.param1 = value;
        }

        /**
         * Gets the value of the param2 property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getParam2() {
            return param2;
        }

        /**
         * Sets the value of the param2 property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setParam2(String value) {
            this.param2 = value;
        }

        /**
         * Gets the value of the param3 property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getParam3() {
            return param3;
        }

        /**
         * Sets the value of the param3 property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setParam3(String value) {
            this.param3 = value;
        }

        /**
         * Gets the value of the param4 property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getParam4() {
            return param4;
        }

        /**
         * Sets the value of the param4 property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setParam4(String value) {
            this.param4 = value;
        }

        /**
         * Gets the value of the param5 property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getParam5() {
            return param5;
        }

        /**
         * Sets the value of the param5 property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setParam5(String value) {
            this.param5 = value;
        }

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
     *         &lt;element name="statusFlg" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
     *         &lt;element name="statusCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="messageText" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="param_1" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="param_2" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
        "statusFlg",
        "statusCode",
        "messageText",
        "param1",
        "param2"
    })
    public static class ReturnComHeader implements ReturnComHeaderIF {

        /**
		 *
		 */
		private static final long serialVersionUID = 2145842407558695052L;

		protected Boolean statusFlg;
        protected String statusCode;
        protected String messageText;
        @XmlElement(name = "param_1")
        protected String param1;
        @XmlElement(name = "param_2")
        protected String param2;

        public String toString() {
    		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
        }

        /**
         * Gets the value of the statusFlg property.
         *
         * @return
         *     possible object is
         *     {@link Boolean }
         *
         */
        public Boolean isStatusFlg() {
            return statusFlg;
        }

        /**
         * Sets the value of the statusFlg property.
         *
         * @param value
         *     allowed object is
         *     {@link Boolean }
         *
         */
        public void setStatusFlg(Boolean value) {
            this.statusFlg = value;
        }

        /**
         * Gets the value of the statusCode property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getStatusCode() {
            return statusCode;
        }

        /**
         * Sets the value of the statusCode property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setStatusCode(String value) {
            this.statusCode = value;
        }

        /**
         * Gets the value of the messageText property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getMessageText() {
            return messageText;
        }

        /**
         * Sets the value of the messageText property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setMessageText(String value) {
            this.messageText = value;
        }

        /**
         * Gets the value of the param1 property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getParam1() {
            return param1;
        }

        /**
         * Sets the value of the param1 property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setParam1(String value) {
            this.param1 = value;
        }

        /**
         * Gets the value of the param2 property.
         *
         * @return
         *     possible object is
         *     {@link String }
         *
         */
        public String getParam2() {
            return param2;
        }

        /**
         * Sets the value of the param2 property.
         *
         * @param value
         *     allowed object is
         *     {@link String }
         *
         */
        public void setParam2(String value) {
            this.param2 = value;
        }

    }

}
