
package com.yodobashi.esa.cms.getupdatedata;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.yodobashi.esa.community.common.COMMONINPUT;


/**
 * <p>Java class for GetUpdateData_Req complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetUpdateData_Req">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="dataType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="dataID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="COMMON_INPUT" type="{http://esa.yodobashi.com/CMS/common}COMMON_INPUT" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetUpdateData_Req", propOrder = {
    "dataType",
    "dataID",
    "commoninput"
})
public class GetUpdateDataReq {

    protected String dataType;
    protected String dataID;
    @XmlElement(name = "COMMON_INPUT")
    protected COMMONINPUT commoninput = new COMMONINPUT();
    
    public GetUpdateDataReq(){}
    
    public GetUpdateDataReq(String dataType, String dataID, String externalSystem, String traceID){
    	this.dataType = dataType;
    	this.dataID = dataID;
    	this.commoninput.setExternalSystem(externalSystem);
    	this.commoninput.setTraceID(traceID);
    }
    
    public GetUpdateDataReq(String dataType, String dataID, String externalSystem, String traceID, String terminalID){
    	this.dataType = dataType;
    	this.dataID = dataID;
    	this.commoninput.setExternalSystem(externalSystem);
    	this.commoninput.setTraceID(traceID);
    	this.commoninput.setTerminalID(terminalID);
    }

    /**
     * Gets the value of the dataType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDataType() {
        return dataType;
    }

    /**
     * Sets the value of the dataType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDataType(String value) {
        this.dataType = value;
    }

    /**
     * Gets the value of the dataID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDataID() {
        return dataID;
    }

    /**
     * Sets the value of the dataID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDataID(String value) {
        this.dataID = value;
    }

    /**
     * Gets the value of the commoninput property.
     * 
     * @return
     *     possible object is
     *     {@link COMMONINPUT }
     *     
     */
    public COMMONINPUT getCOMMONINPUT() {
        return commoninput;
    }

    /**
     * Sets the value of the commoninput property.
     * 
     * @param value
     *     allowed object is
     *     {@link COMMONINPUT }
     *     
     */
    public void setCOMMONINPUT(COMMONINPUT value) {
        this.commoninput = value;
    }

}
