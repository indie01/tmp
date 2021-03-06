package com.yodobashi.esa.auth.validateauthsession;

import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service;

/**
 * This class was generated by Apache CXF 2.7.5
 * 2013-08-15T18:20:19.274+09:00
 * Generated source version: 2.7.5
 * 
 */
@WebServiceClient(name = "ValidateAuthSessionV2Service", 
                  wsdlLocation = "ValidateAuthSessionV2(ECSystem2)20110610.wsdl",
                  targetNamespace = "http://esa.yodobashi.com/AUTH/ValidateAuthSession") 
public class ValidateAuthSessionV2Service extends Service {

    public final static URL WSDL_LOCATION;

    public final static QName SERVICE = new QName("http://esa.yodobashi.com/AUTH/ValidateAuthSession", "ValidateAuthSessionV2Service");
    public final static QName ValidateAuthSessionV2Port = new QName("http://esa.yodobashi.com/AUTH/ValidateAuthSession", "ValidateAuthSessionV2Port");
    static {
        URL url = ValidateAuthSessionV2Service.class.getResource("ValidateAuthSessionV2(ECSystem2)20110610.wsdl");
        if (url == null) {
            url = ValidateAuthSessionV2Service.class.getClassLoader().getResource("ValidateAuthSessionV2(ECSystem2)20110610.wsdl");
        } 
        if (url == null) {
            java.util.logging.Logger.getLogger(ValidateAuthSessionV2Service.class.getName())
                .log(java.util.logging.Level.INFO, 
                     "Can not initialize the default wsdl from {0}", "ValidateAuthSessionV2(ECSystem2)20110610.wsdl");
        }       
        WSDL_LOCATION = url;
    }

    public ValidateAuthSessionV2Service(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public ValidateAuthSessionV2Service(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public ValidateAuthSessionV2Service() {
        super(WSDL_LOCATION, SERVICE);
    }
    

    /**
     *
     * @return
     *     returns ValidateAuthSessionV2
     */
    @WebEndpoint(name = "ValidateAuthSessionV2Port")
    public ValidateAuthSessionV2 getValidateAuthSessionV2Port() {
        return super.getPort(ValidateAuthSessionV2Port, ValidateAuthSessionV2.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns ValidateAuthSessionV2
     */
    @WebEndpoint(name = "ValidateAuthSessionV2Port")
    public ValidateAuthSessionV2 getValidateAuthSessionV2Port(WebServiceFeature... features) {
        return super.getPort(ValidateAuthSessionV2Port, ValidateAuthSessionV2.class, features);
    }

}
