package com.kickmogu.yodobashi.community.service;

import java.util.Date;
import java.util.List;

import com.kickmogu.yodobashi.community.service.vo.ReviewPointSummaryVO;

public interface ReviewPointSummaryService {
	
	void insertReviewPointSummary(List<ReviewPointSummaryVO> list);

	List<ReviewPointSummaryVO> ｇetProductInfomation(
			String[] skus);

	List<String> findModifyProductInformation(
			Date start, Date end);
}
