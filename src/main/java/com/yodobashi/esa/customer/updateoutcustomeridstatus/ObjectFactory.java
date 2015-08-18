
package com.yodobashi.esa.customer.updateoutcustomeridstatus;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.yodobashi.esa.customer.updateoutcustomeridstatus package. 
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

    private final static QName _UpdateOutCustomerIDStatusReq_QNAME = new QName("http://esa.yodobashi.com/Customer/UpdateOutCustomerIDStatus", "UpdateOutCustomerIDStatus_Req");
    private final static QName _UpdateOutCustomerIDStatusResponse_QNAME = new QName("http://esa.yodobashi.com/Customer/UpdateOutCustomerIDStatus", "UpdateOutCustomerIDStatus_Response");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.yodobashi.esa.customer.updateoutcustomeridstatus
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link UpdateOutCustomerIDStatusResponse.OuterCustomerList }
     * 
     */
    public UpdateOutCustomerIDStatusResponse.OuterCustomerList createUpdateOutCustomerIDStatusResponseOuterCustomerList() {
        return new UpdateOutCustomerIDStatusResponse.OuterCustomerList();
    }

    /**
     * Create an instance of {@link UpdateOutCustomerIDStatusResponse }
     * 
     */
    public UpdateOutCustomerIDStatusResponse createUpdateOutCustomerIDStatusResponse() {
        return new UpdateOutCustomerIDStatusResponse();
    }

    /**
     * Create an instance of {@link UpdateOutCustomerIDStatusReq.OuterCustomerList }
     * 
     */
    public UpdateOutCustomerIDStatusReq.OuterCustomerList createUpdateOutCustomerIDStatusReqOuterCustomerList() {
        return new UpdateOutCustomerIDStatusReq.OuterCustomerList();
    }

    /**
     * Create an instance of {@link UpdateOutCustomerIDStatusReq }
     * 
     */
    public UpdateOutCustomerIDStatusReq createUpdateOutCustomerIDStatusReq() {
        return new UpdateOutCustomerIDStatusReq();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateOutCustomerIDStatusReq }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://esa.yodobashi.com/Customer/UpdateOutCustomerIDStatus", name = "UpdateOutCustomerIDStatus_Req")
    public JAXBElement<UpdateOutCustomerIDStatusReq> createUpdateOutCustomerIDStatusReq(UpdateOutCustomerIDStatusReq value) {
        return new JAXBElement<UpdateOutCustomerIDStatusReq>(_UpdateOutCustomerIDStatusReq_QNAME, UpdateOutCustomerIDStatusReq.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateOutCustomerIDStatusResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://esa.yodobashi.com/Customer/UpdateOutCustomerIDStatus", name = "UpdateOutCustomerIDStatus_Response")
    public JAXBElement<UpdateOutCustomerIDStatusResponse> createUpdateOutCustomerIDStatusResponse(UpdateOutCustomerIDStatusResponse value) {
        return new JAXBElement<UpdateOutCustomerIDStatusResponse>(_UpdateOutCustomerIDStatusResponse_QNAME, UpdateOutCustomerIDStatusResponse.class, null, value);
    }

}
