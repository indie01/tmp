package com.yodobashi.esa.customer.sendhtmlmail;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * This class was generated by Apache CXF 2.4.2
 * 2012-02-09T10:17:29.994+09:00
 * Generated source version: 2.4.2
 * 
 */
@WebService(targetNamespace = "http://esa.yodobashi.com/Customer/SendHTMLMail", name = "SendHTMLMail")
@XmlSeeAlso({ObjectFactory.class, com.yodobashi.esa.customer.structure.ObjectFactory.class})
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public interface SendHTMLMail {

    @WebResult(name = "SendHTMLMail_Response", targetNamespace = "http://esa.yodobashi.com/Customer/SendHTMLMail", partName = "SendHTMLMail_Response")
    @WebMethod(operationName = "SendHTMLMail", action = "http://sap.com/xi/WebService/soap1.1")
    public SendHTMLMailResponse sendHTMLMail(
        @WebParam(partName = "SendHTMLMail_Req", name = "SendHTMLMail_Req", targetNamespace = "http://esa.yodobashi.com/Customer/SendHTMLMail")
        SendHTMLMailReq sendHTMLMailReq
    );
}
