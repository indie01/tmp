
package com.yodobashi.esa.customer.createoutcustomerid;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.yodobashi.esa.customer.createoutcustomerid package. 
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

    private final static QName _CreateOutCustomerIDResponse_QNAME = new QName("http://esa.yodobashi.com/Customer/CreateOutCustomerID", "CreateOutCustomerID_Response");
    private final static QName _CreateOutCustomerIDReq_QNAME = new QName("http://esa.yodobashi.com/Customer/CreateOutCustomerID", "CreateOutCustomerID_Req");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.yodobashi.esa.customer.createoutcustomerid
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CreateOutCustomerIDResponse }
     * 
     */
    public CreateOutCustomerIDResponse createCreateOutCustomerIDResponse() {
        return new CreateOutCustomerIDResponse();
    }

    /**
     * Create an instance of {@link CreateOutCustomerIDReq }
     * 
     */
    public CreateOutCustomerIDReq createCreateOutCustomerIDReq() {
        return new CreateOutCustomerIDReq();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreateOutCustomerIDResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://esa.yodobashi.com/Customer/CreateOutCustomerID", name = "CreateOutCustomerID_Response")
    public JAXBElement<CreateOutCustomerIDResponse> createCreateOutCustomerIDResponse(CreateOutCustomerIDResponse value) {
        return new JAXBElement<CreateOutCustomerIDResponse>(_CreateOutCustomerIDResponse_QNAME, CreateOutCustomerIDResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreateOutCustomerIDReq }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://esa.yodobashi.com/Customer/CreateOutCustomerID", name = "CreateOutCustomerID_Req")
    public JAXBElement<CreateOutCustomerIDReq> createCreateOutCustomerIDReq(CreateOutCustomerIDReq value) {
        return new JAXBElement<CreateOutCustomerIDReq>(_CreateOutCustomerIDReq_QNAME, CreateOutCustomerIDReq.class, null, value);
    }

}
