
package com.yodobashi.esa.auth.validateauthsession;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.yodobashi.esa.auth.type.AuthReturn;
import com.yodobashi.esa.common.COMMONRETURN;


/**
 * <p>ValidateAuthSessionV2_Response complex typeのJavaクラス。
 * 
 * <p>次のスキーマ・フラグメントは、このクラス内に含まれる予期されるコンテンツを指定します。
 * 
 * <pre>
 * &lt;complexType name="ValidateAuthSessionV2_Response">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="AuthReturn" type="{http://esa.yodobashi.com/AUTH/type}AuthReturn" minOccurs="0"/>
 *         &lt;element name="COMMON_RETURN" type="{http://esa.yodobashi.com/common}COMMON_RETURN" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ValidateAuthSessionV2_Response", propOrder = {
    "authReturn",
    "commonreturn"
})
public class ValidateAuthSessionV2Response {

    @XmlElement(name = "AuthReturn")
    protected AuthReturn authReturn;
    @XmlElement(name = "COMMON_RETURN")
    protected COMMONRETURN commonreturn;

    /**
     * authReturnプロパティの値を取得します。
     * 
     * @return
     *     possible object is
     *     {@link AuthReturn }
     *     
     */
    public AuthReturn getAuthReturn() {
        return authReturn;
    }

    /**
     * authReturnプロパティの値を設定します。
     * 
     * @param value
     *     allowed object is
     *     {@link AuthReturn }
     *     
     */
    public void setAuthReturn(AuthReturn value) {
        this.authReturn = value;
    }

    /**
     * commonreturnプロパティの値を取得します。
     * 
     * @return
     *     possible object is
     *     {@link COMMONRETURN }
     *     
     */
    public COMMONRETURN getCOMMONRETURN() {
        return commonreturn;
    }

    /**
     * commonreturnプロパティの値を設定します。
     * 
     * @param value
     *     allowed object is
     *     {@link COMMONRETURN }
     *     
     */
    public void setCOMMONRETURN(COMMONRETURN value) {
        this.commonreturn = value;
    }

}
