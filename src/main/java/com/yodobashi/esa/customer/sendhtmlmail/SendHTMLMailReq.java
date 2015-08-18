
package com.yodobashi.esa.customer.sendhtmlmail;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.yodobashi.esa.customer.structure.COMMONINPUT;


/**
 * <p>Java class for SendHTMLMail_Req complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SendHTMLMail_Req">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="mailType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="mailFormat" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="outerCustomerType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="outerCustomerId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="subjectList" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="no" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="text" minOccurs="0">
 *                     &lt;simpleType>
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                         &lt;whiteSpace value="preserve"/>
 *                       &lt;/restriction>
 *                     &lt;/simpleType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="htmlBodyList" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="no" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="text" minOccurs="0">
 *                     &lt;simpleType>
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                         &lt;whiteSpace value="preserve"/>
 *                       &lt;/restriction>
 *                     &lt;/simpleType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="textBodyList" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="no" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                   &lt;element name="text" minOccurs="0">
 *                     &lt;simpleType>
 *                       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                         &lt;whiteSpace value="preserve"/>
 *                       &lt;/restriction>
 *                     &lt;/simpleType>
 *                   &lt;/element>
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
@XmlType(name = "SendHTMLMail_Req", propOrder = {
    "mailType",
    "mailFormat",
    "outerCustomerType",
    "outerCustomerId",
    "subjectList",
    "htmlBodyList",
    "textBodyList",
    "commoninput"
})
public class SendHTMLMailReq {

    protected String mailType;
    protected String mailFormat;
    protected String outerCustomerType;
    protected String outerCustomerId;
    protected List<SendHTMLMailReq.SubjectList> subjectList;
    protected List<SendHTMLMailReq.HtmlBodyList> htmlBodyList;
    protected List<SendHTMLMailReq.TextBodyList> textBodyList;
    @XmlElement(name = "COMMON_INPUT")
    protected COMMONINPUT commoninput;

    /**
     * Gets the value of the mailType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMailType() {
        return mailType;
    }

    /**
     * Sets the value of the mailType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMailType(String value) {
        this.mailType = value;
    }

    /**
     * Gets the value of the mailFormat property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMailFormat() {
        return mailFormat;
    }

    /**
     * Sets the value of the mailFormat property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMailFormat(String value) {
        this.mailFormat = value;
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
     * Gets the value of the subjectList property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the subjectList property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSubjectList().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SendHTMLMailReq.SubjectList }
     * 
     * 
     */
    public List<SendHTMLMailReq.SubjectList> getSubjectList() {
        if (subjectList == null) {
            subjectList = new ArrayList<SendHTMLMailReq.SubjectList>();
        }
        return this.subjectList;
    }

    /**
     * Gets the value of the htmlBodyList property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the htmlBodyList property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHtmlBodyList().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SendHTMLMailReq.HtmlBodyList }
     * 
     * 
     */
    public List<SendHTMLMailReq.HtmlBodyList> getHtmlBodyList() {
        if (htmlBodyList == null) {
            htmlBodyList = new ArrayList<SendHTMLMailReq.HtmlBodyList>();
        }
        return this.htmlBodyList;
    }

    /**
     * Gets the value of the textBodyList property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the textBodyList property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTextBodyList().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SendHTMLMailReq.TextBodyList }
     * 
     * 
     */
    public List<SendHTMLMailReq.TextBodyList> getTextBodyList() {
        if (textBodyList == null) {
            textBodyList = new ArrayList<SendHTMLMailReq.TextBodyList>();
        }
        return this.textBodyList;
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
     *         &lt;element name="no" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="text" minOccurs="0">
     *           &lt;simpleType>
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *               &lt;whiteSpace value="preserve"/>
     *             &lt;/restriction>
     *           &lt;/simpleType>
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
        "no",
        "text"
    })
    public static class HtmlBodyList {

        protected String no;
        protected String text;

        /**
         * Gets the value of the no property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getNo() {
            return no;
        }

        /**
         * Sets the value of the no property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setNo(String value) {
            this.no = value;
        }

        /**
         * Gets the value of the text property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getText() {
            return text;
        }

        /**
         * Sets the value of the text property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setText(String value) {
            this.text = value;
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
     *         &lt;element name="no" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="text" minOccurs="0">
     *           &lt;simpleType>
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *               &lt;whiteSpace value="preserve"/>
     *             &lt;/restriction>
     *           &lt;/simpleType>
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
        "no",
        "text"
    })
    public static class SubjectList {

        protected String no;
        protected String text;

        /**
         * Gets the value of the no property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getNo() {
            return no;
        }

        /**
         * Sets the value of the no property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setNo(String value) {
            this.no = value;
        }

        /**
         * Gets the value of the text property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getText() {
            return text;
        }

        /**
         * Sets the value of the text property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setText(String value) {
            this.text = value;
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
     *         &lt;element name="no" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
     *         &lt;element name="text" minOccurs="0">
     *           &lt;simpleType>
     *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
     *               &lt;whiteSpace value="preserve"/>
     *             &lt;/restriction>
     *           &lt;/simpleType>
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
        "no",
        "text"
    })
    public static class TextBodyList {

        protected String no;
        protected String text;

        /**
         * Gets the value of the no property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getNo() {
            return no;
        }

        /**
         * Sets the value of the no property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setNo(String value) {
            this.no = value;
        }

        /**
         * Gets the value of the text property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getText() {
            return text;
        }

        /**
         * Sets the value of the text property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setText(String value) {
            this.text = value;
        }

    }

}
