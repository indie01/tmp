
package com.yodobashi.esa.auth.validateauthsession;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.yodobashi.esa.auth.validateauthsession package. 
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

    private final static QName _ValidateAuthSessionResponse_QNAME = new QName("http://esa.yodobashi.com/AUTH/ValidateAuthSession", "ValidateAuthSession_Response");
    private final static QName _ValidateAuthSessionReq_QNAME = new QName("http://esa.yodobashi.com/AUTH/ValidateAuthSession", "ValidateAuthSession_Req");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.yodobashi.esa.auth.validateauthsession
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ValidateAuthSessionResponse }
     * 
     */
    public ValidateAuthSessionResponse createValidateAuthSessionResponse() {
        return new ValidateAuthSessionResponse();
    }

    /**
     * Create an instance of {@link ValidateAuthSessionReq }
     * 
     */
    public ValidateAuthSessionReq createValidateAuthSessionReq() {
        return new ValidateAuthSessionReq();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ValidateAuthSessionResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://esa.yodobashi.com/AUTH/ValidateAuthSession", name = "ValidateAuthSession_Response")
    public JAXBElement<ValidateAuthSessionResponse> createValidateAuthSessionResponse(ValidateAuthSessionResponse value) {
        return new JAXBElement<ValidateAuthSessionResponse>(_ValidateAuthSessionResponse_QNAME, ValidateAuthSessionResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ValidateAuthSessionReq }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://esa.yodobashi.com/AUTH/ValidateAuthSession", name = "ValidateAuthSession_Req")
    public JAXBElement<ValidateAuthSessionReq> createValidateAuthSessionReq(ValidateAuthSessionReq value) {
        return new JAXBElement<ValidateAuthSessionReq>(_ValidateAuthSessionReq_QNAME, ValidateAuthSessionReq.class, null, value);
    }

}
