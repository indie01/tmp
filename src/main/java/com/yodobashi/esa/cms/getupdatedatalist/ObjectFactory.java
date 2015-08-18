
package com.yodobashi.esa.cms.getupdatedatalist;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.yodobashi.esa.cms.getupdatedatalist package. 
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

    private final static QName _GetUpdateDataListResponse_QNAME = new QName("http://esa.yodobashi.com/CMS/GetUpdateDataList", "GetUpdateDataList_Response");
    private final static QName _GetUpdateDataListReq_QNAME = new QName("http://esa.yodobashi.com/CMS/GetUpdateDataList", "GetUpdateDataList_Req");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.yodobashi.esa.cms.getupdatedatalist
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GetUpdateDataListResponse.DataList.RecordList }
     * 
     */
    public GetUpdateDataListResponse.DataList.RecordList createGetUpdateDataListResponseDataListRecordList() {
        return new GetUpdateDataListResponse.DataList.RecordList();
    }

    /**
     * Create an instance of {@link GetUpdateDataListResponse.DataList.RecordList.ColumnList }
     * 
     */
    public GetUpdateDataListResponse.DataList.RecordList.ColumnList createGetUpdateDataListResponseDataListRecordListColumnList() {
        return new GetUpdateDataListResponse.DataList.RecordList.ColumnList();
    }

    /**
     * Create an instance of {@link GetUpdateDataListResponse.DataList }
     * 
     */
    public GetUpdateDataListResponse.DataList createGetUpdateDataListResponseDataList() {
        return new GetUpdateDataListResponse.DataList();
    }

    /**
     * Create an instance of {@link GetUpdateDataListResponse.DataList.RecordList.ColumnList.Column }
     * 
     */
    public GetUpdateDataListResponse.DataList.RecordList.ColumnList.Column createGetUpdateDataListResponseDataListRecordListColumnListColumn() {
        return new GetUpdateDataListResponse.DataList.RecordList.ColumnList.Column();
    }

    /**
     * Create an instance of {@link GetUpdateDataListResponse }
     * 
     */
    public GetUpdateDataListResponse createGetUpdateDataListResponse() {
        return new GetUpdateDataListResponse();
    }

    /**
     * Create an instance of {@link GetUpdateDataListReq }
     * 
     */
    public GetUpdateDataListReq createGetUpdateDataListReq() {
        return new GetUpdateDataListReq();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetUpdateDataListResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://esa.yodobashi.com/CMS/GetUpdateDataList", name = "GetUpdateDataList_Response")
    public JAXBElement<GetUpdateDataListResponse> createGetUpdateDataListResponse(GetUpdateDataListResponse value) {
        return new JAXBElement<GetUpdateDataListResponse>(_GetUpdateDataListResponse_QNAME, GetUpdateDataListResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetUpdateDataListReq }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://esa.yodobashi.com/CMS/GetUpdateDataList", name = "GetUpdateDataList_Req")
    public JAXBElement<GetUpdateDataListReq> createGetUpdateDataListReq(GetUpdateDataListReq value) {
        return new JAXBElement<GetUpdateDataListReq>(_GetUpdateDataListReq_QNAME, GetUpdateDataListReq.class, null, value);
    }

}
