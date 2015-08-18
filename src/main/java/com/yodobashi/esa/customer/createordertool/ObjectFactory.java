
package com.yodobashi.esa.customer.createordertool;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.yodobashi.esa.customer.createordertool package. 
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

    private final static QName _CreateOrderToolReq_QNAME = new QName("http://esa.yodobashi.com/Customer/CreateOrderTool", "CreateOrderTool_Req");
    private final static QName _CreateOrderToolResponse_QNAME = new QName("http://esa.yodobashi.com/Customer/CreateOrderTool", "CreateOrderTool_Response");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.yodobashi.esa.customer.createordertool
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CreateOrderToolResponse }
     * 
     */
    public CreateOrderToolResponse createCreateOrderToolResponse() {
        return new CreateOrderToolResponse();
    }

    /**
     * Create an instance of {@link CreateOrderToolReq }
     * 
     */
    public CreateOrderToolReq createCreateOrderToolReq() {
        return new CreateOrderToolReq();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreateOrderToolReq }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://esa.yodobashi.com/Customer/CreateOrderTool", name = "CreateOrderTool_Req")
    public JAXBElement<CreateOrderToolReq> createCreateOrderToolReq(CreateOrderToolReq value) {
        return new JAXBElement<CreateOrderToolReq>(_CreateOrderToolReq_QNAME, CreateOrderToolReq.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreateOrderToolResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://esa.yodobashi.com/Customer/CreateOrderTool", name = "CreateOrderTool_Response")
    public JAXBElement<CreateOrderToolResponse> createCreateOrderToolResponse(CreateOrderToolResponse value) {
        return new JAXBElement<CreateOrderToolResponse>(_CreateOrderToolResponse_QNAME, CreateOrderToolResponse.class, null, value);
    }

}
