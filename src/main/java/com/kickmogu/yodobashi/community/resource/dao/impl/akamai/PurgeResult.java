/**
 * PurgeResult.java
 *
 * このファイルはWSDLから自動生成されました / [en]-(This file was auto-generated from WSDL)
 * Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java生成器によって / [en]-(by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.)
 */

package com.kickmogu.yodobashi.community.resource.dao.impl.akamai;

@SuppressWarnings("serial")
public class PurgeResult  implements java.io.Serializable {
    private int resultCode;

    private java.lang.String resultMsg;

    private java.lang.String sessionID;

    private int estTime;

    private int uriIndex;

    private java.lang.String[] modifiers;

    public PurgeResult() {
    }

    public PurgeResult(
           int resultCode,
           java.lang.String resultMsg,
           java.lang.String sessionID,
           int estTime,
           int uriIndex,
           java.lang.String[] modifiers) {
           this.resultCode = resultCode;
           this.resultMsg = resultMsg;
           this.sessionID = sessionID;
           this.estTime = estTime;
           this.uriIndex = uriIndex;
           this.modifiers = modifiers;
    }


    /**
     * Gets the resultCode value for this PurgeResult.
     * 
     * @return resultCode
     */
    public int getResultCode() {
        return resultCode;
    }


    /**
     * Sets the resultCode value for this PurgeResult.
     * 
     * @param resultCode
     */
    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }


    /**
     * Gets the resultMsg value for this PurgeResult.
     * 
     * @return resultMsg
     */
    public java.lang.String getResultMsg() {
        return resultMsg;
    }


    /**
     * Sets the resultMsg value for this PurgeResult.
     * 
     * @param resultMsg
     */
    public void setResultMsg(java.lang.String resultMsg) {
        this.resultMsg = resultMsg;
    }


    /**
     * Gets the sessionID value for this PurgeResult.
     * 
     * @return sessionID
     */
    public java.lang.String getSessionID() {
        return sessionID;
    }


    /**
     * Sets the sessionID value for this PurgeResult.
     * 
     * @param sessionID
     */
    public void setSessionID(java.lang.String sessionID) {
        this.sessionID = sessionID;
    }


    /**
     * Gets the estTime value for this PurgeResult.
     * 
     * @return estTime
     */
    public int getEstTime() {
        return estTime;
    }


    /**
     * Sets the estTime value for this PurgeResult.
     * 
     * @param estTime
     */
    public void setEstTime(int estTime) {
        this.estTime = estTime;
    }


    /**
     * Gets the uriIndex value for this PurgeResult.
     * 
     * @return uriIndex
     */
    public int getUriIndex() {
        return uriIndex;
    }


    /**
     * Sets the uriIndex value for this PurgeResult.
     * 
     * @param uriIndex
     */
    public void setUriIndex(int uriIndex) {
        this.uriIndex = uriIndex;
    }


    /**
     * Gets the modifiers value for this PurgeResult.
     * 
     * @return modifiers
     */
    public java.lang.String[] getModifiers() {
        return modifiers;
    }


    /**
     * Sets the modifiers value for this PurgeResult.
     * 
     * @param modifiers
     */
    public void setModifiers(java.lang.String[] modifiers) {
        this.modifiers = modifiers;
    }

    private java.lang.Object __equalsCalc = null;
    @SuppressWarnings("unused")
	public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof PurgeResult)) return false;
        PurgeResult other = (PurgeResult) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            this.resultCode == other.getResultCode() &&
            ((this.resultMsg==null && other.getResultMsg()==null) || 
             (this.resultMsg!=null &&
              this.resultMsg.equals(other.getResultMsg()))) &&
            ((this.sessionID==null && other.getSessionID()==null) || 
             (this.sessionID!=null &&
              this.sessionID.equals(other.getSessionID()))) &&
            this.estTime == other.getEstTime() &&
            this.uriIndex == other.getUriIndex() &&
            ((this.modifiers==null && other.getModifiers()==null) || 
             (this.modifiers!=null &&
              java.util.Arrays.equals(this.modifiers, other.getModifiers())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        _hashCode += getResultCode();
        if (getResultMsg() != null) {
            _hashCode += getResultMsg().hashCode();
        }
        if (getSessionID() != null) {
            _hashCode += getSessionID().hashCode();
        }
        _hashCode += getEstTime();
        _hashCode += getUriIndex();
        if (getModifiers() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getModifiers());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getModifiers(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // メタデータ型 / [en]-(Type metadata)
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(PurgeResult.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.akamai.com/purge", "PurgeResult"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("resultCode");
        elemField.setXmlName(new javax.xml.namespace.QName("", "resultCode"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("resultMsg");
        elemField.setXmlName(new javax.xml.namespace.QName("", "resultMsg"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("sessionID");
        elemField.setXmlName(new javax.xml.namespace.QName("", "sessionID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("estTime");
        elemField.setXmlName(new javax.xml.namespace.QName("", "estTime"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("uriIndex");
        elemField.setXmlName(new javax.xml.namespace.QName("", "uriIndex"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("modifiers");
        elemField.setXmlName(new javax.xml.namespace.QName("", "modifiers"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * メタデータオブジェクトの型を返却 / [en]-(Return type metadata object)
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    @SuppressWarnings("rawtypes")
	public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    @SuppressWarnings("rawtypes")
	public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
