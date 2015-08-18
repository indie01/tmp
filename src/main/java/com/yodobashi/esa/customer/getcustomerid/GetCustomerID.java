package com.yodobashi.esa.customer.getcustomerid;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * This class was generated by Apache CXF 2.4.2
 * 2012-03-14T18:30:59.847+09:00
 * Generated source version: 2.4.2
 * 
 */
@WebService(targetNamespace = "http://esa.yodobashi.com/Customer/GetCustomerID", name = "GetCustomerID")
@XmlSeeAlso({com.yodobashi.esa.customer.structure.ObjectFactory.class, ObjectFactory.class})
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public interface GetCustomerID {

    @WebResult(name = "GetCustomerID_Response", targetNamespace = "http://esa.yodobashi.com/Customer/GetCustomerID", partName = "GetCustomerID_Response")
    @WebMethod(operationName = "GetCustomerID", action = "http://sap.com/xi/WebService/soap1.1")
    public GetCustomerIDResponse getCustomerID(
        @WebParam(partName = "GetCustomerID_Req", name = "GetCustomerID_Req", targetNamespace = "http://esa.yodobashi.com/Customer/GetCustomerID")
        GetCustomerIDReq getCustomerIDReq
    );
}
