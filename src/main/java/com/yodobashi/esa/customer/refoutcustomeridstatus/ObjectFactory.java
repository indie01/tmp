
package com.yodobashi.esa.customer.refoutcustomeridstatus;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.yodobashi.esa.customer.refoutcustomeridstatus package. 
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

    private final static QName _RefOutCustomerIDStatusReq_QNAME = new QName("http://esa.yodobashi.com/Customer/RefOutCustomerIDStatus", "RefOutCustomerIDStatus_Req");
    private final static QName _RefOutCustomerIDStatusResponse_QNAME = new QName("http://esa.yodobashi.com/Customer/RefOutCustomerIDStatus", "RefOutCustomerIDStatus_Response");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.yodobashi.esa.customer.refoutcustomeridstatus
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link RefOutCustomerIDStatusReq }
     * 
     */
    public RefOutCustomerIDStatusReq createRefOutCustomerIDStatusReq() {
        return new RefOutCustomerIDStatusReq();
    }

    /**
     * Create an instance of {@link RefOutCustomerIDStatusResponse.OuterCustomerList }
     * 
     */
    public RefOutCustomerIDStatusResponse.OuterCustomerList createRefOutCustomerIDStatusResponseOuterCustomerList() {
        return new RefOutCustomerIDStatusResponse.OuterCustomerList();
    }

    /**
     * Create an instance of {@link RefOutCustomerIDStatusReq.OuterCustomerList }
     * 
     */
    public RefOutCustomerIDStatusReq.OuterCustomerList createRefOutCustomerIDStatusReqOuterCustomerList() {
        return new RefOutCustomerIDStatusReq.OuterCustomerList();
    }

    /**
     * Create an instance of {@link RefOutCustomerIDStatusResponse }
     * 
     */
    public RefOutCustomerIDStatusResponse createRefOutCustomerIDStatusResponse() {
        return new RefOutCustomerIDStatusResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RefOutCustomerIDStatusReq }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://esa.yodobashi.com/Customer/RefOutCustomerIDStatus", name = "RefOutCustomerIDStatus_Req")
    public JAXBElement<RefOutCustomerIDStatusReq> createRefOutCustomerIDStatusReq(RefOutCustomerIDStatusReq value) {
        return new JAXBElement<RefOutCustomerIDStatusReq>(_RefOutCustomerIDStatusReq_QNAME, RefOutCustomerIDStatusReq.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RefOutCustomerIDStatusResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://esa.yodobashi.com/Customer/RefOutCustomerIDStatus", name = "RefOutCustomerIDStatus_Response")
    public JAXBElement<RefOutCustomerIDStatusResponse> createRefOutCustomerIDStatusResponse(RefOutCustomerIDStatusResponse value) {
        return new JAXBElement<RefOutCustomerIDStatusResponse>(_RefOutCustomerIDStatusResponse_QNAME, RefOutCustomerIDStatusResponse.class, null, value);
    }

}
