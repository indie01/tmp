
package com.yodobashi.esa.cms.getupdatedata;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.yodobashi.esa.cms.getupdatedata package. 
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

    private final static QName _GetUpdateDataResponse_QNAME = new QName("http://esa.yodobashi.com/CMS/GetUpdateData", "GetUpdateData_Response");
    private final static QName _GetUpdateDataReq_QNAME = new QName("http://esa.yodobashi.com/CMS/GetUpdateData", "GetUpdateData_Req");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.yodobashi.esa.cms.getupdatedata
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GetUpdateDataReq }
     * 
     */
    public GetUpdateDataReq createGetUpdateDataReq() {
        return new GetUpdateDataReq();
    }

    /**
     * Create an instance of {@link GetUpdateDataResponse.RecordList }
     * 
     */
    public GetUpdateDataResponse.RecordList createGetUpdateDataResponseRecordList() {
        return new GetUpdateDataResponse.RecordList();
    }

    /**
     * Create an instance of {@link GetUpdateDataResponse.RecordList.ColumnList.Column }
     * 
     */
    public GetUpdateDataResponse.RecordList.ColumnList.Column createGetUpdateDataResponseRecordListColumnListColumn() {
        return new GetUpdateDataResponse.RecordList.ColumnList.Column();
    }

    /**
     * Create an instance of {@link GetUpdateDataResponse.RecordList.ColumnList }
     * 
     */
    public GetUpdateDataResponse.RecordList.ColumnList createGetUpdateDataResponseRecordListColumnList() {
        return new GetUpdateDataResponse.RecordList.ColumnList();
    }

    /**
     * Create an instance of {@link GetUpdateDataResponse }
     * 
     */
    public GetUpdateDataResponse createGetUpdateDataResponse() {
        return new GetUpdateDataResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetUpdateDataResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://esa.yodobashi.com/CMS/GetUpdateData", name = "GetUpdateData_Response")
    public JAXBElement<GetUpdateDataResponse> createGetUpdateDataResponse(GetUpdateDataResponse value) {
        return new JAXBElement<GetUpdateDataResponse>(_GetUpdateDataResponse_QNAME, GetUpdateDataResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetUpdateDataReq }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://esa.yodobashi.com/CMS/GetUpdateData", name = "GetUpdateData_Req")
    public JAXBElement<GetUpdateDataReq> createGetUpdateDataReq(GetUpdateDataReq value) {
        return new JAXBElement<GetUpdateDataReq>(_GetUpdateDataReq_QNAME, GetUpdateDataReq.class, null, value);
    }

}
