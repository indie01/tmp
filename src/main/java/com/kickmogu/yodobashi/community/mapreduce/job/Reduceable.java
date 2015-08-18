package com.kickmogu.yodobashi.community.mapreduce.job;

import java.io.IOException;

import org.apache.hadoop.mapreduce.Reducer.Context;

public interface Reduceable {
	@SuppressWarnings("rawtypes")
	public void doRun(Context context) throws IOException, InterruptedException;
}
