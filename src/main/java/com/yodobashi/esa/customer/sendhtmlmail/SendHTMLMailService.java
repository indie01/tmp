package com.yodobashi.esa.customer.sendhtmlmail;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service;

/**
 * This class was generated by Apache CXF 2.4.2
 * 2012-02-09T10:17:30.010+09:00
 * Generated source version: 2.4.2
 * 
 */
@WebServiceClient(name = "SendHTMLMailService", 
                  wsdlLocation = "http://euc3sx1:8000/sap/xi/engine?type=entry&version=3.0&Sender.Service=Community1&Interface=http%3A%2F%2Fesa.yodobashi.com%2FCustomer%2FSendHTMLMail%5ESendHTMLMail",
                  targetNamespace = "http://esa.yodobashi.com/Customer/SendHTMLMail") 
public class SendHTMLMailService extends Service {

    public final static URL WSDL_LOCATION;

    public final static QName SERVICE = new QName("http://esa.yodobashi.com/Customer/SendHTMLMail", "SendHTMLMailService");
    public final static QName SendHTMLMailPort = new QName("http://esa.yodobashi.com/Customer/SendHTMLMail", "SendHTMLMailPort");
    static {
        URL url = null;
        try {
            url = new URL("http://euc3sx1:8000/sap/xi/engine?type=entry&version=3.0&Sender.Service=Community1&Interface=http%3A%2F%2Fesa.yodobashi.com%2FCustomer%2FSendHTMLMail%5ESendHTMLMail");
        } catch (MalformedURLException e) {
            java.util.logging.Logger.getLogger(SendHTMLMailService.class.getName())
                .log(java.util.logging.Level.INFO, 
                     "Can not initialize the default wsdl from {0}", "http://euc3sx1:8000/sap/xi/engine?type=entry&version=3.0&Sender.Service=Community1&Interface=http%3A%2F%2Fesa.yodobashi.com%2FCustomer%2FSendHTMLMail%5ESendHTMLMail");
        }
        WSDL_LOCATION = url;
    }

    public SendHTMLMailService(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public SendHTMLMailService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public SendHTMLMailService() {
        super(WSDL_LOCATION, SERVICE);
    }
    

    /**
     *
     * @return
     *     returns SendHTMLMail
     */
    @WebEndpoint(name = "SendHTMLMailPort")
    public SendHTMLMail getSendHTMLMailPort() {
        return super.getPort(SendHTMLMailPort, SendHTMLMail.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns SendHTMLMail
     */
    @WebEndpoint(name = "SendHTMLMailPort")
    public SendHTMLMail getSendHTMLMailPort(WebServiceFeature... features) {
        return super.getPort(SendHTMLMailPort, SendHTMLMail.class, features);
    }

}
