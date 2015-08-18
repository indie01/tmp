
package com.yodobashi.esa.usm.deleteuniversalsession;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.yodobashi.esa.usm.deleteuniversalsession package. 
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

    private final static QName _DeleteUniversalSessionResponse_QNAME = new QName("http://esa.yodobashi.com/USM/DeleteUniversalSession", "DeleteUniversalSession_Response");
    private final static QName _DeleteUniversalSessionReq_QNAME = new QName("http://esa.yodobashi.com/USM/DeleteUniversalSession", "DeleteUniversalSession_Req");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.yodobashi.esa.usm.deleteuniversalsession
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link DeleteUniversalSessionResponse }
     * 
     */
    public DeleteUniversalSessionResponse createDeleteUniversalSessionResponse() {
        return new DeleteUniversalSessionResponse();
    }

    /**
     * Create an instance of {@link DeleteUniversalSessionReq }
     * 
     */
    public DeleteUniversalSessionReq createDeleteUniversalSessionReq() {
        return new DeleteUniversalSessionReq();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeleteUniversalSessionResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://esa.yodobashi.com/USM/DeleteUniversalSession", name = "DeleteUniversalSession_Response")
    public JAXBElement<DeleteUniversalSessionResponse> createDeleteUniversalSessionResponse(DeleteUniversalSessionResponse value) {
        return new JAXBElement<DeleteUniversalSessionResponse>(_DeleteUniversalSessionResponse_QNAME, DeleteUniversalSessionResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeleteUniversalSessionReq }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://esa.yodobashi.com/USM/DeleteUniversalSession", name = "DeleteUniversalSession_Req")
    public JAXBElement<DeleteUniversalSessionReq> createDeleteUniversalSessionReq(DeleteUniversalSessionReq value) {
        return new JAXBElement<DeleteUniversalSessionReq>(_DeleteUniversalSessionReq_QNAME, DeleteUniversalSessionReq.class, null, value);
    }

}
