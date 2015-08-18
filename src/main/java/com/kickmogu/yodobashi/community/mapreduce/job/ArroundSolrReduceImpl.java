package com.kickmogu.yodobashi.community.mapreduce.job;

import java.io.IOException;

import org.apache.hadoop.mapreduce.Reducer.Context;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.solr.annotation.ArroundSolr;

@Service("arroundSolrReduceImpl") @Scope("prototype")
public class ArroundSolrReduceImpl implements ReduceIF {

	@SuppressWarnings("rawtypes")
	@Override
	@ArroundSolr
	public void doRun(Reduceable reduceable, Context context) throws IOException,
			InterruptedException {
		reduceable.doRun(context);
	}


}
