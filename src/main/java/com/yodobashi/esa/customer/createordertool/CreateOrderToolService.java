package com.yodobashi.esa.customer.createordertool;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service;

/**
 * This class was generated by Apache CXF 2.4.2
 * 2012-04-24T17:03:43.274+09:00
 * Generated source version: 2.4.2
 * 
 */
@WebServiceClient(name = "CreateOrderToolService", 
                  wsdlLocation = "http://euc3sx1:8000/sap/xi/engine?type=entry&version=3.0&Sender.Service=Community1&Interface=http%3A%2F%2Fesa.yodobashi.com%2FCustomer%2FCreateOrderTool%5ECreateOrderTool",
                  targetNamespace = "http://esa.yodobashi.com/Customer/CreateOrderTool") 
public class CreateOrderToolService extends Service {

    public final static URL WSDL_LOCATION;

    public final static QName SERVICE = new QName("http://esa.yodobashi.com/Customer/CreateOrderTool", "CreateOrderToolService");
    public final static QName CreateOrderToolPort = new QName("http://esa.yodobashi.com/Customer/CreateOrderTool", "CreateOrderToolPort");
    static {
        URL url = null;
        try {
            url = new URL("http://euc3sx1:8000/sap/xi/engine?type=entry&version=3.0&Sender.Service=Community1&Interface=http%3A%2F%2Fesa.yodobashi.com%2FCustomer%2FCreateOrderTool%5ECreateOrderTool");
        } catch (MalformedURLException e) {
            java.util.logging.Logger.getLogger(CreateOrderToolService.class.getName())
                .log(java.util.logging.Level.INFO, 
                     "Can not initialize the default wsdl from {0}", "http://euc3sx1:8000/sap/xi/engine?type=entry&version=3.0&Sender.Service=Community1&Interface=http%3A%2F%2Fesa.yodobashi.com%2FCustomer%2FCreateOrderTool%5ECreateOrderTool");
        }
        WSDL_LOCATION = url;
    }

    public CreateOrderToolService(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public CreateOrderToolService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public CreateOrderToolService() {
        super(WSDL_LOCATION, SERVICE);
    }
    

    /**
     *
     * @return
     *     returns CreateOrderTool
     */
    @WebEndpoint(name = "CreateOrderToolPort")
    public CreateOrderTool getCreateOrderToolPort() {
        return super.getPort(CreateOrderToolPort, CreateOrderTool.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns CreateOrderTool
     */
    @WebEndpoint(name = "CreateOrderToolPort")
    public CreateOrderTool getCreateOrderToolPort(WebServiceFeature... features) {
        return super.getPort(CreateOrderToolPort, CreateOrderTool.class, features);
    }

}