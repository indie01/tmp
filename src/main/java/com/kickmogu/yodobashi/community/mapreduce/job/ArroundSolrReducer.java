package com.kickmogu.yodobashi.community.mapreduce.job;

import java.io.IOException;

import org.apache.hadoop.mapreduce.Reducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public class ArroundSolrReducer<KEYIN,VALUEIN,KEYOUT,VALUEOUT> extends Reducer<KEYIN, VALUEIN, KEYOUT, VALUEOUT>  implements Reduceable {

	@Autowired
	private ApplicationContext applicationContext;

	@SuppressWarnings("rawtypes")
	@Override
	public void run(org.apache.hadoop.mapreduce.Reducer.Context context)
			throws IOException, InterruptedException {
		applicationContext.getBean("arroundSolrReduceImpl",ReduceIF.class).doRun(this, context);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void doRun(org.apache.hadoop.mapreduce.Reducer.Context context)
			throws IOException, InterruptedException {
		super.run(context);
	}


}
