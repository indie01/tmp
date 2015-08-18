/**
 * JavaClasses.java
 *
 * このファイルはWSDLから自動生成されました / [en]-(This file was auto-generated from WSDL)
 * Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java生成器によって / [en]-(by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.)
 */

package com.kickmogu.yodobashi.community.resource.dao.impl.akamai;

public interface JavaClasses extends javax.xml.rpc.Service {

/**
 * Provides programmatic purge access
 */
    public java.lang.String getPurgeApiAddress();

    public com.kickmogu.yodobashi.community.resource.dao.impl.akamai.PurgeApi getPurgeApi() throws javax.xml.rpc.ServiceException;

    public com.kickmogu.yodobashi.community.resource.dao.impl.akamai.PurgeApi getPurgeApi(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
