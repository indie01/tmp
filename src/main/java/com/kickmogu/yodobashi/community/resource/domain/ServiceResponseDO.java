package com.kickmogu.yodobashi.community.resource.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.yodobashi.esa.community.common.COMMONRETURN;

@XmlRootElement(name="ServiceResponse")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
name = "ServiceResponse",
namespace="http://esa.yodobashi.com/COMMUNITY/types",
propOrder = {
    "commonreturn"
}
)
public class ServiceResponseDO {
	
	public static final ServiceResponseDO SUCCESS = new ServiceResponseDO(COMMONRETURN.SUCCESS);
	
	public ServiceResponseDO(){}
	
	public ServiceResponseDO(COMMONRETURN commonreturn){
		this.commonreturn = commonreturn;
	}
	
	@XmlElement(name = "COMMON_RETURN")
	private COMMONRETURN commonreturn;


    public COMMONRETURN getCOMMONRETURN() {
        return commonreturn;
    }

    public void setCOMMONRETURN(COMMONRETURN value) {
        this.commonreturn = value;
    }
    
    public String toString() {
    	return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
    	.append("commonreturn", commonreturn)
    	.toString();
    }

}
