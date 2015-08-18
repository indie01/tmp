/**
 * JavaClassesLocator.java
 *
 * このファイルはWSDLから自動生成されました / [en]-(This file was auto-generated from WSDL)
 * Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java生成器によって / [en]-(by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.)
 */

package com.kickmogu.yodobashi.community.resource.dao.impl.akamai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JavaClassesLocator extends org.apache.axis.client.Service implements com.kickmogu.yodobashi.community.resource.dao.impl.akamai.JavaClasses {

/**
	 *
	 */
	private static final long serialVersionUID = -429175546294575094L;

/**
 * Provides programmatic purge access
 */

    public JavaClassesLocator() {
    }


    public JavaClassesLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public JavaClassesLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // PurgeApiのプロキシクラスの取得に使用します / [en]-(Use to get a proxy class for PurgeApi)
    @Value("${endpoint.akamai}")
    private java.lang.String PurgeApi_address;

    public java.lang.String getPurgeApiAddress() {
        return PurgeApi_address;
    }

    // WSDDサービス名のデフォルトはポート名です / [en]-(The WSDD service name defaults to the port name.)
    private java.lang.String PurgeApiWSDDServiceName = "PurgeApi";

    public java.lang.String getPurgeApiWSDDServiceName() {
        return PurgeApiWSDDServiceName;
    }

    public void setPurgeApiWSDDServiceName(java.lang.String name) {
        PurgeApiWSDDServiceName = name;
    }

    public com.kickmogu.yodobashi.community.resource.dao.impl.akamai.PurgeApi getPurgeApi() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(PurgeApi_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getPurgeApi(endpoint);
    }

    public com.kickmogu.yodobashi.community.resource.dao.impl.akamai.PurgeApi getPurgeApi(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.kickmogu.yodobashi.community.resource.dao.impl.akamai.PurgeApiSOAPBindingStub _stub = new com.kickmogu.yodobashi.community.resource.dao.impl.akamai.PurgeApiSOAPBindingStub(portAddress, this);
            _stub.setPortName(getPurgeApiWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setPurgeApiEndpointAddress(java.lang.String address) {
        PurgeApi_address = address;
    }

    /**
     * 与えられたインターフェースに対して、スタブの実装を取得します。 / [en]-(For the given interface, get the stub implementation.)
     * このサービスが与えられたインターフェースに対してポートを持たない場合、 / [en]-(If this service has no port for the given interface,)
     * ServiceExceptionが投げられます。 / [en]-(then ServiceException is thrown.)
     */
    @SuppressWarnings("rawtypes")
	public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (com.kickmogu.yodobashi.community.resource.dao.impl.akamai.PurgeApi.class.isAssignableFrom(serviceEndpointInterface)) {
                com.kickmogu.yodobashi.community.resource.dao.impl.akamai.PurgeApiSOAPBindingStub _stub = new com.kickmogu.yodobashi.community.resource.dao.impl.akamai.PurgeApiSOAPBindingStub(new java.net.URL(PurgeApi_address), this);
                _stub.setPortName(getPurgeApiWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("インターフェースに対するスタブの実装がありません: / [en]-(There is no stub implementation for the interface:)  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * 与えられたインターフェースに対して、スタブの実装を取得します。 / [en]-(For the given interface, get the stub implementation.)
     * このサービスが与えられたインターフェースに対してポートを持たない場合、 / [en]-(If this service has no port for the given interface,)
     * ServiceExceptionが投げられます。 / [en]-(then ServiceException is thrown.)
     */
    @SuppressWarnings("rawtypes")
	public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("PurgeApi".equals(inputPortName)) {
            return getPurgeApi();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://www.akamai.com/purge", "JavaClasses");
    }

    @SuppressWarnings("rawtypes")
	private java.util.HashSet ports = null;

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://www.akamai.com/purge", "PurgeApi"));
        }
        return ports.iterator();
    }

    /**
    * 指定したポート名に対するエンドポイントのアドレスをセットします / [en]-(Set the endpoint address for the specified port name.)
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {

if ("PurgeApi".equals(portName)) {
            setPurgeApiEndpointAddress(address);
        }
        else
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" 未知のポートに対してはエンドポイントのアドレスをセットできません / [en]-(Cannot set Endpoint Address for Unknown Port)" + portName);
        }
    }

    /**
    * 指定したポート名に対するエンドポイントのアドレスをセットします / [en]-(Set the endpoint address for the specified port name.)
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
