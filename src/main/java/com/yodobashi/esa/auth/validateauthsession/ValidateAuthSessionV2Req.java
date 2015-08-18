
package com.yodobashi.esa.auth.validateauthsession;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.yodobashi.esa.auth.type.NvPair;
import com.yodobashi.esa.common.COMMONINPUT;


/**
 * <p>ValidateAuthSessionV2_Req complex typeのJavaクラス。
 * 
 * <p>次のスキーマ・フラグメントは、このクラス内に含まれる予期されるコンテンツを指定します。
 * 
 * <pre>
 * &lt;complexType name="ValidateAuthSessionV2_Req">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="authSessionID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="newTimeout" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="nvPair" type="{http://esa.yodobashi.com/AUTH/type}nvPair" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="COMMON_INPUT" type="{http://esa.yodobashi.com/common}COMMON_INPUT" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ValidateAuthSessionV2_Req", propOrder = {
    "authSessionID",
    "newTimeout",
    "nvPair",
    "commoninput"
})
public class ValidateAuthSessionV2Req {

    protected String authSessionID;
    protected Integer newTimeout;
    protected List<NvPair> nvPair;
    @XmlElement(name = "COMMON_INPUT")
    protected COMMONINPUT commoninput;

    /**
     * authSessionIDプロパティの値を取得します。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAuthSessionID() {
        return authSessionID;
    }

    /**
     * authSessionIDプロパティの値を設定します。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAuthSessionID(String value) {
        this.authSessionID = value;
    }

    /**
     * newTimeoutプロパティの値を取得します。
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getNewTimeout() {
        return newTimeout;
    }

    /**
     * newTimeoutプロパティの値を設定します。
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setNewTimeout(Integer value) {
        this.newTimeout = value;
    }

    /**
     * Gets the value of the nvPair property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the nvPair property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNvPair().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link NvPair }
     * 
     * 
     */
    public List<NvPair> getNvPair() {
        if (nvPair == null) {
            nvPair = new ArrayList<NvPair>();
        }
        return this.nvPair;
    }

    /**
     * commoninputプロパティの値を取得します。
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
     * commoninputプロパティの値を設定します。
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
