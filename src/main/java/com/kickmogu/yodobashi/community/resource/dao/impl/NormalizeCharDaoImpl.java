/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.kickmogu.lib.core.utils.StringUtil;
import com.kickmogu.lib.hadoop.hbase.HBaseOperations;
import com.kickmogu.yodobashi.community.resource.dao.NormalizeCharDao;
import com.kickmogu.yodobashi.community.resource.domain.NormalizeCharDO;
import com.kickmogu.yodobashi.community.resource.domain.SpoofingNameDO;


/**
 * 標準化文字 DAO の実装クラスです。
 * @author kamiike
 *
 */
@Service
public class NormalizeCharDaoImpl implements NormalizeCharDao,InitializingBean {

	/**
	 * HBaseアクセサです。
	 */
	@Autowired  @Qualifier("default")
	private HBaseOperations hBaseOperations;
	
	private List<NormalizeCharDO> normalizeCharList;

	/**
	 * 指定したテキストを標準化します。
	 * @param text テキスト
	 * @return 標準化されたテキスト
	 */
	@Override
	public String normalizeString(String text) {
		if (text == null) {
			return null;
		}
		String sameSummaryText = normalizeSamePattern(text);

		
		/* 20120627 コミュニティニックネーム重複対応 */
/*		
		String chageParts = sameSummaryText;
		String afterParts = "";

		if (sameSummaryText.length() > MAX_CHECK_LENGTH) {
			chageParts = sameSummaryText.substring(0, MAX_CHECK_LENGTH);
			afterParts = sameSummaryText.substring(MAX_CHECK_LENGTH);
		}


		StringBuilder buffer = new StringBuilder();
		while (chageParts.length() > 0) {
			NormalizeCharDO replaceChar = null;
			for (NormalizeCharDO normalizeChar : normalizeCharList) {
				if (chageParts.startsWith((normalizeChar.getCharacter()))) {
					replaceChar = normalizeChar;
					break;
				}
			}
			if (replaceChar == null) {
				buffer.append(chageParts.substring(0, 1));
				chageParts = chageParts.substring(1);
			} else {
				chageParts = chageParts.substring(replaceChar.getCharacter().length());
				buffer.append(" ");
				buffer.append(replaceChar.getGroup().getGroupId());
				buffer.append(" ");
			}
		}
		buffer.append(afterParts);

		return buffer.toString();
		*/
		
		return sameSummaryText;
	}

	public String getSpoofingPattern(String text){
		
		String chageParts = text;
		
		StringBuilder buffer = new StringBuilder();
		while (chageParts.length() > 0) {
			NormalizeCharDO replaceChar = null;
			for (NormalizeCharDO normalizeChar : normalizeCharList) {
				if (chageParts.startsWith((normalizeChar.getCharacter()))) {
					replaceChar = normalizeChar;
					break;
				}
			}
			if (replaceChar == null) {
				return null;
			} else {
				chageParts = chageParts.substring(replaceChar.getCharacter().length());
				buffer.append(" ");
				buffer.append(replaceChar.getGroup().getGroupId());
				buffer.append(" ");
			}
		}
		return buffer.toString();
	}

	public boolean validateSpoofingPattern(String text, boolean withLock){
		
		if(StringUtils.isEmpty(text)){
			return true;
		}
		SpoofingNameDO spoofing = null;
		if (withLock) {
			spoofing = hBaseOperations.loadWithLock(SpoofingNameDO.class, StringUtil.toSHA256(text));
		} else {
			spoofing = hBaseOperations.load(SpoofingNameDO.class, StringUtil.toSHA256(text));
		}
		  
		if(spoofing == null){
			return true;
		}
		return false;
	}
	
	/**
	 * 同一性パターンを標準化します。
	 * @param text テキスト
	 * @return 標準化されたテキスト
	 */
	private String normalizeSamePattern(String text) {
		String result = text;

		//英数字、カタカナ、記号について半角に変換
		result = StringUtil.zenkakuToHankakuAll(result);
		//カナ小文字（ｧ、ｨ、ｩ・・・）を大文字（ｱ、ｲ、ｳ）に変換
		result = StringUtil.hankakuKatakanaSmallToLarge(result);
		//不要文字（スペース）削除
		result = result.replaceAll("\\s", "");
		result = result.replace("　", "");
		result = result.replace(".", "");
		result = result.replace(",", "");
		result = result.replace(":", "");
		result = result.replace(";", "");
		return result;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		normalizeCharList = hBaseOperations.scanAll(
				NormalizeCharDO.class);

		Collections.sort(normalizeCharList, new Comparator<NormalizeCharDO>() {

			@Override
			public int compare(NormalizeCharDO o1, NormalizeCharDO o2) {
				return o1.getOrderNo() - o2.getOrderNo();
			}

		});
	}
}
