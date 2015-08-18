/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao.impl;

import java.text.SimpleDateFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.core.aop.TimestampHolder;
import com.kickmogu.lib.core.resource.Path;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.yodobashi.community.resource.dao.DailyScoreFactorDao;
import com.kickmogu.yodobashi.community.resource.dao.util.IdUtil;
import com.kickmogu.yodobashi.community.resource.domain.DailyScoreFactorDO;

/**
 * 日次スコア要因 DAO です。
 * @author kamiike
 *
 */
@Service
public class DailyScoreFactorDaoImpl implements DailyScoreFactorDao {

	/**
	 * HBaseアクセサです。
	 */
	@Autowired  @Qualifier("default")
	private HBaseOperations hBaseOperations;

	/**
	 * タイムスタンプホルダーです。
	 */
	@Autowired
	private TimestampHolder timestampHolder;

	/**
	 * 日次スコア要因を登録します。
	 * @param factor 日次スコア要因
	 */
	@Override
	public void createDailyScoreFactor(DailyScoreFactorDO factor) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		factor.setDailyScoreFactorId(IdUtil.createIdByConcatIds(
				 factor.getContentsId(),
				factor.getType().getCode(), formatter.format(factor.getTargetDate())));
		factor.setRegisterDateTime(timestampHolder.getTimestamp());
		factor.setModifyDateTime(timestampHolder.getTimestamp());
		hBaseOperations.save(factor);
	}
	
	private BulkUpdate<DailyScoreFactorDO> bulkUpdate = null;
	@Override
	public void createDailyScoreFactorForBatchBegin(int bulkSize){
		bulkUpdate = new BulkUpdate<DailyScoreFactorDO>(DailyScoreFactorDO.class, 
				hBaseOperations, null, Path.DEFAULT, bulkSize);
	}
	@Override
	public void createDailyScoreFactorForBatch(DailyScoreFactorDO factor){
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		factor.setDailyScoreFactorId(IdUtil.createIdByConcatIds(
				 factor.getContentsId(),
				factor.getType().getCode(), formatter.format(factor.getTargetDate())));
		factor.setRegisterDateTime(timestampHolder.getTimestamp());
		factor.setModifyDateTime(timestampHolder.getTimestamp());
		
		bulkUpdate.write(factor);
	}
	@Override
	public void createDailyScoreFactorForBatchEnd(){
		bulkUpdate.end();
	}

}
