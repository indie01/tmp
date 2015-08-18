
package com.yodobashi.esa.customer.getoutcustomerid;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.yodobashi.esa.customer.getoutcustomerid package. 
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

    private final static QName _GetOutCustomerIDReq_QNAME = new QName("http://esa.yodobashi.com/Customer/GetOutCustomerID", "GetOutCustomerID_Req");
    private final static QName _GetOutCustomerIDResponse_QNAME = new QName("http://esa.yodobashi.com/Customer/GetOutCustomerID", "GetOutCustomerID_Response");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.yodobashi.esa.customer.getoutcustomerid
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GetOutCustomerIDResponse }
     * 
     */
    public GetOutCustomerIDResponse createGetOutCustomerIDResponse() {
        return new GetOutCustomerIDResponse();
    }

    /**
     * Create an instance of {@link GetOutCustomerIDResponse.OuterCustomerList }
     * 
     */
    public GetOutCustomerIDResponse.OuterCustomerList createGetOutCustomerIDResponseOuterCustomerList() {
        return new GetOutCustomerIDResponse.OuterCustomerList();
    }

    /**
     * Create an instance of {@link GetOutCustomerIDReq }
     * 
     */
    public GetOutCustomerIDReq createGetOutCustomerIDReq() {
        return new GetOutCustomerIDReq();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetOutCustomerIDReq }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://esa.yodobashi.com/Customer/GetOutCustomerID", name = "GetOutCustomerID_Req")
    public JAXBElement<GetOutCustomerIDReq> createGetOutCustomerIDReq(GetOutCustomerIDReq value) {
        return new JAXBElement<GetOutCustomerIDReq>(_GetOutCustomerIDReq_QNAME, GetOutCustomerIDReq.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetOutCustomerIDResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://esa.yodobashi.com/Customer/GetOutCustomerID", name = "GetOutCustomerID_Response")
    public JAXBElement<GetOutCustomerIDResponse> createGetOutCustomerIDResponse(GetOutCustomerIDResponse value) {
        return new JAXBElement<GetOutCustomerIDResponse>(_GetOutCustomerIDResponse_QNAME, GetOutCustomerIDResponse.class, null, value);
    }

}
