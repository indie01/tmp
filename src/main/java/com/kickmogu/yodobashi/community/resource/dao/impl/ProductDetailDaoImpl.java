/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Service;

import com.kickmogu.yodobashi.community.resource.cache.MethodCache;
import com.kickmogu.yodobashi.community.resource.cache.CacheStrategyType;
import com.kickmogu.yodobashi.community.resource.cache.TargetSystemType;
import com.kickmogu.yodobashi.community.resource.dao.ProductDetailDao;
import com.kickmogu.yodobashi.community.resource.domain.constants.ProductType;

/**
 * 商品詳細情報を直接読み出すための DAO の実装です。
 * @author kamiike
 */
@Service @Lazy @Qualifier("default")
public class ProductDetailDaoImpl implements ProductDetailDao,InitializingBean  {

	Map<String, String[]> janCodeProductMap;

	/**
	 * テンプレートです。
	 */
	@Autowired @Qualifier("default")
	private JdbcTemplate template;

	/**
	 * SKUマップを返します。
	 * @param janCodes JANコードリスト
	 * @return SKUマップ（key=janCode、value={sku, productType, adultKind}）
	 */
	@Override
	@MethodCache(
			cacheStrategy=CacheStrategyType.JavaVMGlobal,
			size=1000,
			limitTime=60,
			limitTimeUnit=TimeUnit.MINUTES,
			targetSystems={TargetSystemType.CommunityWs, TargetSystemType.CommunityJc, TargetSystemType.CommunityBatch, TargetSystemType.CommunityDataSyncWeb}
			)
	public Map<String, String[]> loadSkuMap(List<String> janCodes) {
		final Map<String, String[]> resultMap = new HashMap<String, String[]>();
		if (janCodes == null || janCodes.size() == 0) {
			return resultMap;
		}

		if (janCodeProductMap != null) {
			for (String jan : janCodes) {
				resultMap.put(jan, janCodeProductMap.get(jan));
			}
			return resultMap;
		}

		// 通常品のチェック
		List<List<String>> janCodeSplit = new ArrayList<List<String>>();
		List<String> janCodeArray = new ArrayList<String>();
		
		for (String janCode : janCodes) {
			janCodeArray.add(janCode);
			
			if( janCodeArray.size() == 10 ){
				janCodeSplit.add(janCodeArray);
				janCodeArray = new ArrayList<String>();
			}
		}
		
		if( janCodeArray.size() > 0 ){
			janCodeSplit.add(janCodeArray);
		}
		
		for( List<String >janCodeList : janCodeSplit ){
			StringBuilder singleSql = new StringBuilder();
			singleSql.append("SELECT sku, productType, janCode, adultKind FROM productDetail WHERE janCode IN (");
			for (int i = 0; i < janCodeList.size(); i++) {
				if (i > 0) {
					singleSql.append(", ");
				}
				singleSql.append("?");
			}
			singleSql.append(") AND channelType = '0001'");
			singleSql.append(" AND productType IN (");
			singleSql.append("'" + ProductType.NORMAL.getCode() + "'");
			singleSql.append(", '" + ProductType.DOWNLOAD_WAU.getCode() + "'");
			singleSql.append(", '" + ProductType.DOWNLOAD_WITHOUT_WAU.getCode() + "'");
			singleSql.append(", '" + ProductType.DVD_WAU.getCode() + "'");
			singleSql.append(", '" + ProductType.NORMAL_WITH_JMD.getCode() + "'");
			singleSql.append(", '" + ProductType.DOWNLOAD_NUMBER.getCode() + "'");
			singleSql.append(") AND setProductFlag = 0");
			singleSql.append(" AND familyFlag IN( 0, 2) ");
			singleSql.append(" AND serviceFlag = 0");
			singleSql.append(" AND complexFlag = 0");
			singleSql.append(" AND startTime <= SYSDATE");
			singleSql.append(" AND endTime >= SYSDATE");
			template.query(singleSql.toString(),
				janCodeList.toArray(),
				new RowCallbackHandler() {
					public void processRow(ResultSet rs) throws SQLException {
						resultMap.put(rs.getString("janCode"),
							new String[] {
								rs.getString("sku"),
								rs.getString("productType"),
								rs.getString("adultKind")
							});
						}
					}
				);
		}
		
		List<String> noFindList = new ArrayList<String>();
		for (String janCode : janCodes) {
			if (!resultMap.containsKey(janCode)) {
				noFindList.add(janCode);
			}
		}
		if (noFindList.size() == 0) {
			return resultMap;
		}
		
		janCodeSplit = new ArrayList<List<String>>();
		janCodeArray = new ArrayList<String>();
		
		for (String janCode : noFindList) {
			janCodeArray.add(janCode);
			
			if( janCodeArray.size() == 10 ){
				janCodeSplit.add(janCodeArray);
				janCodeArray = new ArrayList<String>();
			}
		}
		
		if( janCodeArray.size() > 0 ){
			janCodeSplit.add(janCodeArray);
		}
		// セット商品のチェック
		for( List<String >janCodeList : janCodeSplit ){
			StringBuilder setSql = new StringBuilder();
			setSql.append("SELECT pa.sku as sku, pd.janCode as janCode, pd.adultKind as adultKind FROM pAttrRelatedItem pa INNER JOIN (");
			setSql.append("SELECT sku, janCode, adultKind FROM productDetail WHERE janCode IN (");
			for (int i = 0; i < janCodeList.size(); i++) {
				if (i > 0) {
					setSql.append(", ");
				}
				setSql.append("?");
			}
			setSql.append(") AND channelType = '0001'");
			setSql.append(" AND productType = '" + ProductType.NORMAL.getCode() + "'");
			setSql.append(" AND setProductFlag = 1");
			setSql.append(" AND familyFlag = 0");
			setSql.append(" AND serviceFlag = 0");
			setSql.append(" AND complexFlag = 0");
			setSql.append(" AND startTime <= SYSDATE");
			setSql.append(" AND endTime >= SYSDATE");
			setSql.append(") pd ON pa.relatedSku = pd.sku");
			setSql.append(" AND pa.channelType = '0001'");
			setSql.append(" AND pa.relatedpCategoryCode = '0007'");
			setSql.append(" AND pa.startTime <= SYSDATE");
			setSql.append(" AND pa.endTime >= SYSDATE");
			template.query(setSql.toString(),
					janCodeList.toArray(),
					new RowCallbackHandler() {
						public void processRow(ResultSet rs) throws SQLException {
							resultMap.put(rs.getString("janCode"),
									new String[] {
										rs.getString("sku"),
										ProductType.NORMAL.getCode(),
										rs.getString("adultKind")
									});
						}
					});
		}
		noFindList = new ArrayList<String>();
		for (String janCode : janCodes) {
			if (!resultMap.containsKey(janCode)) {
				noFindList.add(janCode);
			}
		}
		if (noFindList.size() == 0) {
			return resultMap;
		}

		
		// 複合品商品のチェック
		final Map<String, String[]> resultComplexMap = new HashMap<String, String[]>();
		
		janCodeSplit = new ArrayList<List<String>>();
		janCodeArray = new ArrayList<String>();
		
		for (String janCode : noFindList) {
			janCodeArray.add(janCode);
			
			if( janCodeArray.size() == 10 ){
				janCodeSplit.add(janCodeArray);
				janCodeArray = new ArrayList<String>();
			}
		}
		
		if( janCodeArray.size() > 0 ){
			janCodeSplit.add(janCodeArray);
		}
		
		for( List<String >janCodeList : janCodeSplit ){
			StringBuilder complexSql = new StringBuilder();
			complexSql.append("SELECT pa.sku as sku, pd.janCode as janCode, pd.adultKind as adultKind FROM pAttrRelatedItem pa INNER JOIN (");
			complexSql.append("SELECT sku, janCode, adultKind FROM productDetail WHERE janCode IN (");
			for (int i = 0; i < janCodeList.size(); i++) {
				if (i > 0) {
					complexSql.append(", ");
				}
				complexSql.append("?");
			}
			complexSql.append(") AND channelType = '0001'");
			complexSql.append(" AND productType = '" + ProductType.NORMAL.getCode() + "'");
			complexSql.append(" AND setProductFlag = 0");
			complexSql.append(" AND familyFlag = 0");
			complexSql.append(" AND serviceFlag = 0");
			complexSql.append(" AND complexFlag = 2");
			complexSql.append(" AND startTime <= SYSDATE");
			complexSql.append(" AND endTime >= SYSDATE");
			complexSql.append(") pd ON pa.relatedSku = pd.sku");
			complexSql.append(" AND pa.channelType = '0001'");
			complexSql.append(" AND pa.relatedpCategoryCode = '0009'");
			complexSql.append(" AND pa.startTime <= SYSDATE");
			complexSql.append(" AND pa.endTime >= SYSDATE");
			template.query(complexSql.toString(),
					janCodeList.toArray(),
					new RowCallbackHandler() {
						public void processRow(ResultSet rs) throws SQLException {
							String key = rs.getString("sku");
							if( resultComplexMap.containsKey(key)){
								// 複合品は２つの商品で構成されているため、重複するSKUがある場合は、正常な組み合わせである。
								String[] value = resultComplexMap.get(key);
								value[2] = "true";
								resultComplexMap.put(key, value);
							}else{
								resultComplexMap.put(key,
										new String[] {
											rs.getString("janCode"),
											rs.getString("adultKind"),
											"false"
										});
							}
						}
					});
		}
		// 複合品組合せのチェック
		if(resultComplexMap.isEmpty())
			return resultMap;
		
		Set<Entry<String, String[]>> keySet = resultComplexMap.entrySet();
		Iterator<Entry<String, String[]>> keyIt =  keySet.iterator();

		while(keyIt.hasNext()){
			Entry<String, String[]> entry = keyIt.next();
			if( "true".equals(entry.getValue()[2])){
				resultMap.put(entry.getValue()[0],
						new String[] {
							entry.getKey(),
							ProductType.NORMAL.getCode(),
							entry.getValue()[1]
						});
			}
		}

		return resultMap;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (System.getProperty("use.file.to.ProductDetailDao") != null) {
			File janListFile = new File(new File(
					"/home/comm/migration-files"),
					"product_by_jan.txt");
			if (janListFile.exists()) {
				janCodeProductMap = new HashMap<String, String[]>();
				BufferedReader reader = null;
				try {
					reader = new BufferedReader(new InputStreamReader(
							new FileInputStream(janListFile)));
					for (String line = reader.readLine();
							line != null; line = reader.readLine()) {
						String[] data = line.split(",", 4);
						janCodeProductMap.put(data[0], new String[] {data[1], data[2], data[3],});
					}
				} finally {
					if (reader != null) {
						reader.close();
					}
				}
			}
		}
	}

}
