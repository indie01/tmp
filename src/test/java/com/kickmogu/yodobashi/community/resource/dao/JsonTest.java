package com.kickmogu.yodobashi.community.resource.dao;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Test;

import com.google.common.collect.Maps;
import com.kickmogu.yodobashi.community.resource.domain.ReviewMetaDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.ReviewMetaType;

public class JsonTest {

	@Test
	public void writeJsonOne() throws JsonGenerationException, JsonMappingException, IOException {
		ReviewMetaDO reviewMetaDO = new ReviewMetaDO();
		reviewMetaDO.setCategoryCode("000000000000000000");
		Map<ReviewMetaType, String> meta = Maps.newHashMap();
		
		meta.put(ReviewMetaType.A001_TITLE, "購入の決め手");
		meta.put(ReviewMetaType.A001_DESCRIPTION, "新しく追加　例）デザイン、価格、使い勝手、機能性など");
		
		meta.put(ReviewMetaType.A002_TITLE, "購入を迷った商品");
		meta.put(ReviewMetaType.A002_DESCRIPTION, "商品名を検索してください");
		
		meta.put(ReviewMetaType.A003_TITLE, "過去に使っていた商品");
		meta.put(ReviewMetaType.A003_DESCRIPTION, "商品名を検索してください");
		
		meta.put(ReviewMetaType.A004_TITLE, "レビュー");
		meta.put(ReviewMetaType.A004_DESCRIPTION, "この商品を選んだ動機、経緯、購入した喜びなどをお書きください");
		
		meta.put(ReviewMetaType.A005_TITLE, "満足度");
		meta.put(ReviewMetaType.A005_DESCRIPTION, "");
		
		meta.put(ReviewMetaType.B001_TITLE, "満足度");
		meta.put(ReviewMetaType.B001_DESCRIPTION, "");
		
		meta.put(ReviewMetaType.B002_TITLE, "この製品を次を買いますか");
		meta.put(ReviewMetaType.B002_DESCRIPTION, "");
		
		meta.put(ReviewMetaType.B003_TITLE, "レビュー");
		meta.put(ReviewMetaType.B003_DESCRIPTION, "この商品を選んだ動機、経緯、購入した喜びなどをお書きください");
		
		reviewMetaDO.setMeta(meta);
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(new File("review-meta.json"), reviewMetaDO);
	}
	
	@Test
	public void readJsonOne() throws JsonParseException, JsonMappingException, IOException{
		ObjectMapper mapper = new ObjectMapper();
		ReviewMetaDO reviewMetaDO = mapper.readValue(new File("review-meta.json"), ReviewMetaDO.class);
		System.out.println(reviewMetaDO.toString());
	}
	
	@Test
	public void writeJsonList() throws JsonGenerationException, JsonMappingException, IOException {
		Map<String, ReviewMetaDO> reviewMetas = Maps.newHashMap();
		ReviewMetaDO reviewMetaDO = new ReviewMetaDO();
		reviewMetaDO.setCategoryCode("000000000000000000");
		Map<ReviewMetaType, String> meta = Maps.newHashMap();
		
		meta.put(ReviewMetaType.A001_TITLE, "購入の決め手");
		meta.put(ReviewMetaType.A001_DESCRIPTION, "新しく追加　例）デザイン、価格、使い勝手、機能性など");
		
		meta.put(ReviewMetaType.A002_TITLE, "購入を迷った商品");
		meta.put(ReviewMetaType.A002_DESCRIPTION, "商品名を検索してください");
		
		meta.put(ReviewMetaType.A003_TITLE, "過去に使っていた商品");
		meta.put(ReviewMetaType.A003_DESCRIPTION, "商品名を検索してください");
		
		meta.put(ReviewMetaType.A004_TITLE, "レビュー");
		meta.put(ReviewMetaType.A004_DESCRIPTION, "この商品を選んだ動機、経緯、購入した喜びなどをお書きください");
		
		meta.put(ReviewMetaType.A005_TITLE, "満足度");
		meta.put(ReviewMetaType.A005_DESCRIPTION, "");
		
		meta.put(ReviewMetaType.B001_TITLE, "満足度");
		meta.put(ReviewMetaType.B001_DESCRIPTION, "");
		
		meta.put(ReviewMetaType.B002_TITLE, "この製品を次を買いますか");
		meta.put(ReviewMetaType.B002_DESCRIPTION, "");
		
		meta.put(ReviewMetaType.B003_TITLE, "レビュー");
		meta.put(ReviewMetaType.B003_DESCRIPTION, "この商品を選んだ動機、経緯、購入した喜びなどをお書きください");
		
		reviewMetaDO.setMeta(meta);
		
		reviewMetas.put(reviewMetaDO.getCategoryCode(), reviewMetaDO);
		
		reviewMetaDO = new ReviewMetaDO();
		
		reviewMetaDO = new ReviewMetaDO();
		reviewMetaDO.setCategoryCode("811000000000000000");
		meta = Maps.newHashMap();
		
		meta.put(ReviewMetaType.A001_TITLE, "購入の決め手");
		meta.put(ReviewMetaType.A001_DESCRIPTION, "新しく追加　例）作者に興味がある、ストーリー、話題性など");
		
		meta.put(ReviewMetaType.A002_TITLE, "購入を迷った書籍");
		meta.put(ReviewMetaType.A002_DESCRIPTION, "書籍名を検索してください");
		
		meta.put(ReviewMetaType.A003_TITLE, "過去に呼んだことのある書籍");
		meta.put(ReviewMetaType.A003_DESCRIPTION, "書籍名を検索してください");
		
		meta.put(ReviewMetaType.A004_TITLE, "レビュー");
		meta.put(ReviewMetaType.A004_DESCRIPTION, "この書籍を選んだ動機、経緯、読んでみた感想などをお書きください");
		
		meta.put(ReviewMetaType.A005_TITLE, "満足度");
		meta.put(ReviewMetaType.A005_DESCRIPTION, "");
		
		meta.put(ReviewMetaType.B001_TITLE, "満足度");
		meta.put(ReviewMetaType.B001_DESCRIPTION, "");
		
		meta.put(ReviewMetaType.B002_TITLE, "この書籍の新刊が発売されたら買いますか");
		meta.put(ReviewMetaType.B002_DESCRIPTION, "");
		
		meta.put(ReviewMetaType.B003_TITLE, "レビュー");
		meta.put(ReviewMetaType.B003_DESCRIPTION, "この書籍を選んだ動機、経緯、読んでみた感想などをお書きください");
		
		reviewMetaDO.setMeta(meta);
		
		reviewMetas.put(reviewMetaDO.getCategoryCode(), reviewMetaDO);
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(new File("review-metas.json"), reviewMetas);
	}
	
	@Test
	public void readJsonList() throws JsonParseException, JsonMappingException, IOException{
		ObjectMapper mapper = new ObjectMapper();
		
		TypeReference<Map<String, ReviewMetaDO>> typeRef = new TypeReference<Map<String, ReviewMetaDO>>(){};
		
		Map<String, ReviewMetaDO> reviewMetas = mapper.readValue(new File("review-metas.json"),typeRef);
		
		System.out.println(reviewMetas.toString());
	}

}
