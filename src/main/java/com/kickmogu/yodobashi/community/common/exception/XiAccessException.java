package com.kickmogu.yodobashi.community.common.exception;

import java.util.List;

import com.kickmogu.lib.core.domain.ReturnComDetailIF;
import com.kickmogu.lib.core.domain.ReturnComHeaderIF;


public class XiAccessException extends YcComException {

	private ReturnComHeaderIF responseHeader;
	private List<? extends ReturnComDetailIF> responseDetail;

	/**
	 *
	 */
	private static final long serialVersionUID = 2179471322748637994L;

	public XiAccessException(String message) {
		super(message);
	}

    public XiAccessException(String message, Throwable cause){
        super(message, cause);
    }

    public XiAccessException(Throwable cause){
        super(cause);
    }


	public XiAccessException(ReturnComHeaderIF responseHeader, List<? extends ReturnComDetailIF> responseDetail) {
		super("StatusFlg=" + responseHeader.isStatusFlg() + " StatusCode=" + responseHeader.getStatusCode());
		this.responseHeader = responseHeader;
		this.responseDetail = responseDetail;
	}

	public ReturnComHeaderIF getResponseHeader() {
		return responseHeader;
	}

	public void setResponseHeader(ReturnComHeaderIF responseHeader) {
		this.responseHeader = responseHeader;
	}

	public List<? extends ReturnComDetailIF> getResponseDetail() {
		return responseDetail;
	}

	public void setResponseDetail(List<? extends ReturnComDetailIF> responseDetail) {
		this.responseDetail = responseDetail;
	}

}
