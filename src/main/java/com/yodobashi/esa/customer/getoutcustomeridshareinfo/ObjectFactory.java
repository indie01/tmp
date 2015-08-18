
package com.yodobashi.esa.customer.getoutcustomeridshareinfo;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.yodobashi.esa.customer.getoutcustomeridshareinfo package. 
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

    private final static QName _GetOutCustomerIDShareInfoResponse_QNAME = new QName("http://esa.yodobashi.com/Customer/GetOutCustomerIDShareInfo", "GetOutCustomerIDShareInfo_Response");
    private final static QName _GetOutCustomerIDShareInfoReq_QNAME = new QName("http://esa.yodobashi.com/Customer/GetOutCustomerIDShareInfo", "GetOutCustomerIDShareInfo_Req");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.yodobashi.esa.customer.getoutcustomeridshareinfo
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GetOutCustomerIDShareInfoReq }
     * 
     */
    public GetOutCustomerIDShareInfoReq createGetOutCustomerIDShareInfoReq() {
        return new GetOutCustomerIDShareInfoReq();
    }

    /**
     * Create an instance of {@link GetOutCustomerIDShareInfoResponse }
     * 
     */
    public GetOutCustomerIDShareInfoResponse createGetOutCustomerIDShareInfoResponse() {
        return new GetOutCustomerIDShareInfoResponse();
    }

    /**
     * Create an instance of {@link GetOutCustomerIDShareInfoResponse.ShareInfoList }
     * 
     */
    public GetOutCustomerIDShareInfoResponse.ShareInfoList createGetOutCustomerIDShareInfoResponseShareInfoList() {
        return new GetOutCustomerIDShareInfoResponse.ShareInfoList();
    }

    /**
     * Create an instance of {@link GetOutCustomerIDShareInfoReq.ShareInfoList }
     * 
     */
    public GetOutCustomerIDShareInfoReq.ShareInfoList createGetOutCustomerIDShareInfoReqShareInfoList() {
        return new GetOutCustomerIDShareInfoReq.ShareInfoList();
    }

    /**
     * Create an instance of {@link GetOutCustomerIDShareInfoResponse.ShareInfoList.OuterCustomerIdShareInfoList }
     * 
     */
    public GetOutCustomerIDShareInfoResponse.ShareInfoList.OuterCustomerIdShareInfoList createGetOutCustomerIDShareInfoResponseShareInfoListOuterCustomerIdShareInfoList() {
        return new GetOutCustomerIDShareInfoResponse.ShareInfoList.OuterCustomerIdShareInfoList();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetOutCustomerIDShareInfoResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://esa.yodobashi.com/Customer/GetOutCustomerIDShareInfo", name = "GetOutCustomerIDShareInfo_Response")
    public JAXBElement<GetOutCustomerIDShareInfoResponse> createGetOutCustomerIDShareInfoResponse(GetOutCustomerIDShareInfoResponse value) {
        return new JAXBElement<GetOutCustomerIDShareInfoResponse>(_GetOutCustomerIDShareInfoResponse_QNAME, GetOutCustomerIDShareInfoResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetOutCustomerIDShareInfoReq }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://esa.yodobashi.com/Customer/GetOutCustomerIDShareInfo", name = "GetOutCustomerIDShareInfo_Req")
    public JAXBElement<GetOutCustomerIDShareInfoReq> createGetOutCustomerIDShareInfoReq(GetOutCustomerIDShareInfoReq value) {
        return new JAXBElement<GetOutCustomerIDShareInfoReq>(_GetOutCustomerIDShareInfoReq_QNAME, GetOutCustomerIDShareInfoReq.class, null, value);
    }

}
