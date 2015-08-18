
package com.yodobashi.esa.usm.searchuniversalsession;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.yodobashi.esa.usm.searchuniversalsession package. 
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

    private final static QName _SearchUniversalSessionReq_QNAME = new QName("http://esa.yodobashi.com/USM/SearchUniversalSession", "SearchUniversalSession_Req");
    private final static QName _SearchUniversalSessionResponse_QNAME = new QName("http://esa.yodobashi.com/USM/SearchUniversalSession", "SearchUniversalSession_Response");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.yodobashi.esa.usm.searchuniversalsession
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link SearchUniversalSessionReq }
     * 
     */
    public SearchUniversalSessionReq createSearchUniversalSessionReq() {
        return new SearchUniversalSessionReq();
    }

    /**
     * Create an instance of {@link SearchUniversalSessionResponse }
     * 
     */
    public SearchUniversalSessionResponse createSearchUniversalSessionResponse() {
        return new SearchUniversalSessionResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SearchUniversalSessionReq }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://esa.yodobashi.com/USM/SearchUniversalSession", name = "SearchUniversalSession_Req")
    public JAXBElement<SearchUniversalSessionReq> createSearchUniversalSessionReq(SearchUniversalSessionReq value) {
        return new JAXBElement<SearchUniversalSessionReq>(_SearchUniversalSessionReq_QNAME, SearchUniversalSessionReq.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SearchUniversalSessionResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://esa.yodobashi.com/USM/SearchUniversalSession", name = "SearchUniversalSession_Response")
    public JAXBElement<SearchUniversalSessionResponse> createSearchUniversalSessionResponse(SearchUniversalSessionResponse value) {
        return new JAXBElement<SearchUniversalSessionResponse>(_SearchUniversalSessionResponse_QNAME, SearchUniversalSessionResponse.class, null, value);
    }

}
