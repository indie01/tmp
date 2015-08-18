package com.kickmogu.yodobashi.community.mapreduce.job;

import java.io.IOException;

import org.apache.hadoop.mapreduce.Reducer.Context;

public interface ReduceIF {
	@SuppressWarnings("rawtypes")
	public void doRun(Reduceable reduceable, Context context) throws IOException, InterruptedException;
}
