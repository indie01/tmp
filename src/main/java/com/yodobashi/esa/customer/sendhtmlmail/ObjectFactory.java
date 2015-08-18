
package com.yodobashi.esa.customer.sendhtmlmail;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.yodobashi.esa.customer.sendhtmlmail package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _SendHTMLMailReq_QNAME = new QName("http://esa.yodobashi.com/Customer/SendHTMLMail", "SendHTMLMail_Req");
    private final static QName _SendHTMLMailResponse_QNAME = new QName("http://esa.yodobashi.com/Customer/SendHTMLMail", "SendHTMLMail_Response");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.yodobashi.esa.customer.sendhtmlmail
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link SendHTMLMailReq }
     * 
     */
    public SendHTMLMailReq createSendHTMLMailReq() {
        return new SendHTMLMailReq();
    }

    /**
     * Create an instance of {@link SendHTMLMailResponse }
     * 
     */
    public SendHTMLMailResponse createSendHTMLMailResponse() {
        return new SendHTMLMailResponse();
    }

    /**
     * Create an instance of {@link SendHTMLMailReq.SubjectList }
     * 
     */
    public SendHTMLMailReq.SubjectList createSendHTMLMailReqSubjectList() {
        return new SendHTMLMailReq.SubjectList();
    }

    /**
     * Create an instance of {@link SendHTMLMailReq.HtmlBodyList }
     * 
     */
    public SendHTMLMailReq.HtmlBodyList createSendHTMLMailReqHtmlBodyList() {
        return new SendHTMLMailReq.HtmlBodyList();
    }

    /**
     * Create an instance of {@link SendHTMLMailReq.TextBodyList }
     * 
     */
    public SendHTMLMailReq.TextBodyList createSendHTMLMailReqTextBodyList() {
        return new SendHTMLMailReq.TextBodyList();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SendHTMLMailReq }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://esa.yodobashi.com/Customer/SendHTMLMail", name = "SendHTMLMail_Req")
    public JAXBElement<SendHTMLMailReq> createSendHTMLMailReq(SendHTMLMailReq value) {
        return new JAXBElement<SendHTMLMailReq>(_SendHTMLMailReq_QNAME, SendHTMLMailReq.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SendHTMLMailResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://esa.yodobashi.com/Customer/SendHTMLMail", name = "SendHTMLMail_Response")
    public JAXBElement<SendHTMLMailResponse> createSendHTMLMailResponse(SendHTMLMailResponse value) {
        return new JAXBElement<SendHTMLMailResponse>(_SendHTMLMailResponse_QNAME, SendHTMLMailResponse.class, null, value);
    }

}
