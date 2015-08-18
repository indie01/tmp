
package com.yodobashi.esa.community.getcommunityupdatedata;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.yodobashi.esa.community.getcommunityupdatedata package. 
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

    private final static QName _GetCommunityUpdateDataReq_QNAME = new QName("http://esa.yodobashi.com/COMMUNITY/GetCommunityUpdateData", "GetCommunityUpdateData_Req");
    private final static QName _GetCommunityUpdateDataResponse_QNAME = new QName("http://esa.yodobashi.com/COMMUNITY/GetCommunityUpdateData", "GetCommunityUpdateData_Response");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.yodobashi.esa.community.getcommunityupdatedata
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GetCommunityUpdateDataResponse }
     * 
     */
    public GetCommunityUpdateDataResponse createGetCommunityUpdateDataResponse() {
        return new GetCommunityUpdateDataResponse();
    }

    /**
     * Create an instance of {@link GetCommunityUpdateDataResponse.DataList.RecordList }
     * 
     */
    public GetCommunityUpdateDataResponse.DataList.RecordList createGetCommunityUpdateDataResponseDataListRecordList() {
        return new GetCommunityUpdateDataResponse.DataList.RecordList();
    }

    /**
     * Create an instance of {@link GetCommunityUpdateDataResponse.DataList }
     * 
     */
    public GetCommunityUpdateDataResponse.DataList createGetCommunityUpdateDataResponseDataList() {
        return new GetCommunityUpdateDataResponse.DataList();
    }

    /**
     * Create an instance of {@link GetCommunityUpdateDataResponse.DataList.RecordList.ColumnList.Column }
     * 
     */
    public GetCommunityUpdateDataResponse.DataList.RecordList.ColumnList.Column createGetCommunityUpdateDataResponseDataListRecordListColumnListColumn() {
        return new GetCommunityUpdateDataResponse.DataList.RecordList.ColumnList.Column();
    }

    /**
     * Create an instance of {@link GetCommunityUpdateDataResponse.DataList.RecordList.ColumnList }
     * 
     */
    public GetCommunityUpdateDataResponse.DataList.RecordList.ColumnList createGetCommunityUpdateDataResponseDataListRecordListColumnList() {
        return new GetCommunityUpdateDataResponse.DataList.RecordList.ColumnList();
    }

    /**
     * Create an instance of {@link GetCommunityUpdateDataReq }
     * 
     */
    public GetCommunityUpdateDataReq createGetCommunityUpdateDataReq() {
        return new GetCommunityUpdateDataReq();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetCommunityUpdateDataReq }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://esa.yodobashi.com/COMMUNITY/GetCommunityUpdateData", name = "GetCommunityUpdateData_Req")
    public JAXBElement<GetCommunityUpdateDataReq> createGetCommunityUpdateDataReq(GetCommunityUpdateDataReq value) {
        return new JAXBElement<GetCommunityUpdateDataReq>(_GetCommunityUpdateDataReq_QNAME, GetCommunityUpdateDataReq.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetCommunityUpdateDataResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://esa.yodobashi.com/COMMUNITY/GetCommunityUpdateData", name = "GetCommunityUpdateData_Response")
    public JAXBElement<GetCommunityUpdateDataResponse> createGetCommunityUpdateDataResponse(GetCommunityUpdateDataResponse value) {
        return new JAXBElement<GetCommunityUpdateDataResponse>(_GetCommunityUpdateDataResponse_QNAME, GetCommunityUpdateDataResponse.class, null, value);
    }

}
