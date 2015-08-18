/**
 * PurgeApi.java
 *
 * このファイルはWSDLから自動生成されました / [en]-(This file was auto-generated from WSDL)
 * Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java生成器によって / [en]-(by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.)
 */

package com.kickmogu.yodobashi.community.resource.dao.impl.akamai;

public interface PurgeApi extends java.rmi.Remote {
    public com.kickmogu.yodobashi.community.resource.dao.impl.akamai.PurgeResult purgeRequest(java.lang.String name, java.lang.String pwd, java.lang.String network, java.lang.String[] opt, java.lang.String[] uri) throws java.rmi.RemoteException;
}
