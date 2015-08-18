
package com.yodobashi.esa.customer.getcustomerid;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.yodobashi.esa.customer.getcustomerid package. 
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

    private final static QName _GetCustomerIDReq_QNAME = new QName("http://esa.yodobashi.com/Customer/GetCustomerID", "GetCustomerID_Req");
    private final static QName _GetCustomerIDResponse_QNAME = new QName("http://esa.yodobashi.com/Customer/GetCustomerID", "GetCustomerID_Response");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.yodobashi.esa.customer.getcustomerid
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GetCustomerIDResponse }
     * 
     */
    public GetCustomerIDResponse createGetCustomerIDResponse() {
        return new GetCustomerIDResponse();
    }

    /**
     * Create an instance of {@link GetCustomerIDReq }
     * 
     */
    public GetCustomerIDReq createGetCustomerIDReq() {
        return new GetCustomerIDReq();
    }

    /**
     * Create an instance of {@link GetCustomerIDResponse.OuterCustomerList }
     * 
     */
    public GetCustomerIDResponse.OuterCustomerList createGetCustomerIDResponseOuterCustomerList() {
        return new GetCustomerIDResponse.OuterCustomerList();
    }

    /**
     * Create an instance of {@link GetCustomerIDReq.CustomerList }
     * 
     */
    public GetCustomerIDReq.CustomerList createGetCustomerIDReqCustomerList() {
        return new GetCustomerIDReq.CustomerList();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetCustomerIDReq }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://esa.yodobashi.com/Customer/GetCustomerID", name = "GetCustomerID_Req")
    public JAXBElement<GetCustomerIDReq> createGetCustomerIDReq(GetCustomerIDReq value) {
        return new JAXBElement<GetCustomerIDReq>(_GetCustomerIDReq_QNAME, GetCustomerIDReq.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetCustomerIDResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://esa.yodobashi.com/Customer/GetCustomerID", name = "GetCustomerID_Response")
    public JAXBElement<GetCustomerIDResponse> createGetCustomerIDResponse(GetCustomerIDResponse value) {
        return new JAXBElement<GetCustomerIDResponse>(_GetCustomerIDResponse_QNAME, GetCustomerIDResponse.class, null, value);
    }

}
