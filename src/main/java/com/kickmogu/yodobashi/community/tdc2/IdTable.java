package com.kickmogu.yodobashi.community.tdc2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.google.common.collect.Lists;
import com.kickmogu.lib.core.exception.CommonSystemException;

class IdTable {
	List<String[]> list = Lists.newArrayList();
	String dim = "\t";
	
	public IdTable() {
		load(new File(getFileName()));
	}
	public IdTable(String filePath) {
		load(new File(filePath));
	}
	void load(File file) {
		list.clear();
		BufferedReader reader = null;
		try {
			String line;
			reader = new BufferedReader(new FileReader(file));
			while ((line = reader.readLine()) != null) {
				list.add(line.trim().split(dim));
			}
		} catch (Throwable e) {
			throw new CommonSystemException(getFileName()+" failed.", e);
		} finally {
			IOUtils.closeQuietly(reader);
		}
	}
	String getFileName() {
		return TestId.DIRECTORY + File.separator +  "sku_list.csv";
	}
	String[] getRecord(int counter) {
		return list.get(counter%list.size());
	}
	
	String[] getOther(int counter) {
		int next = counter%list.size() + 1;
		if (next == list.size()) next = 0;
		return list.get(next);
	}
}
