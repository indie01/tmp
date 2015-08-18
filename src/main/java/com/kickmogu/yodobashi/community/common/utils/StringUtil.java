package com.kickmogu.yodobashi.community.common.utils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

import com.kickmogu.yodobashi.community.common.exception.YcComException;

public class StringUtil {

	public static final int MAX_BYTES = 255;
	public static final String WHITE_SPACE = " ";

	public static String toBase64(byte[] binary) {
		if (binary == null) return null;
		return Base64.encodeBase64String(binary);
	}

	public static String stripTags(String src) {
		if(StringUtils.isEmpty(src)) return "";
		String dest = src.replaceAll("\\t", "");
		dest = dest.replaceAll("\\r", "");
		dest = dest.replaceAll("\\n", "");
		dest = dest.replaceAll(" *<(.+?)> *", "");
		return dest;
	}
	
	/**
	 * 文字列を255byteごとに分け、前後にシングルクォーテーションを付けて配列に格納して返す。
	 *
	 * @param aStr
	 * @return String[]
	 */
	public static String[] storageStringArray(String aStr) {

		final String EMPTY = "";

		if (StringUtils.isBlank(aStr)) {
			return new String[]{EMPTY};
		}

		StringBuffer sb = new StringBuffer();

		if (aStr.length() < (MAX_BYTES / 2)) {
			return new String[]{aStr};
		}

		byte[] bytes = null;
		try {
			bytes = aStr.getBytes("Windows-31J");
		} catch (UnsupportedEncodingException e) {
			throw new YcComException(e);
		}
		List<String> retList = new ArrayList<String>();

		if ( bytes.length <= MAX_BYTES){
			return new String[]{aStr};
		}

		//格納する文字列の何バイト目かのカウンター
		int cnt = 0;

		//文字列の全バイト数のうちの何バイト目かのカウンター。
		int allByteCnt = 0;
		boolean isWhiteSpaceCheck = false;
		
		
		
		for (int i = 0; i < aStr.length(); i++) {
			if (allByteCnt >= bytes.length) {
				break;
			}

			// 前回のリスト内の末尾がWhiteSpaceの場合、今回のリストに含める
			if(isWhiteSpaceCheck && !retList.isEmpty() && retList.size() > 0){
				String src = retList.get(retList.size()-1);
				int beforeLength = 0;
				int afterLength = 0;
				if(src != null){
					beforeLength = src.length();
					afterLength = StringUtils.stripEnd(src, WHITE_SPACE).length();
				}
				for(int x=0;x<beforeLength-afterLength;x++){
					cnt++;
					sb.append(WHITE_SPACE);
				}
				isWhiteSpaceCheck = false;
			}
			
			//今の文字。
			char now = aStr.charAt(i);
			//全角か。
			boolean zenkaku = false;

			//文字列を１文字づつ見ていく。
			if ((bytes[allByteCnt] & 0x80) != 0) {	//→今回は半角かなが無いので、最上位ビットが立っていたら全角。
				zenkaku = true;
				cnt += 2;
				allByteCnt += 2;
			} else {
				cnt++;
				allByteCnt++;
			}

			//カウンターが255バイトを超えたら、今見てる文字列は無視して、最後にシングルクォーテーションを付けてリストに格納する。
			if (cnt > MAX_BYTES ) {
				retList.add(sb.toString());
				//格納文字列、カウンターをリセットする。
				sb = new StringBuffer();
				cnt = 0;
				//この時点では、今みてる文字列はappendされてないので、ループのカウントを１つ減らす。
				i--;
				//↑と同様に、カウントを１つ(全角だったら２つ)減らす。
				if(zenkaku){
					allByteCnt -= 2;
				} else {
					allByteCnt--;
				}
				isWhiteSpaceCheck = true;
				continue;
			}
			sb.append(now);
		}

		//まだリストに格納してない文字があったら、格納する。
		if (sb.length() > 1) {
			retList.add(sb.toString());
		}

		if (retList==null || retList.size() <= 0) {
			return new String[]{EMPTY};
		}
		return (String[]) retList.toArray(new String[retList.size()]);
	}


	/**
	 * コンテンツ登校時に不要なタグが挿入されるバグ対応
	 * どこから挿入されるかは未発見のため該当タグがある場合は排除する。
	 * <div class=\"cntrbtTxt\">コンテンツ本文</div>
	 * &lt;dic class=&quot;cntrbtTxt&quot;コンテンツ本文&lt;/div&gt;
	 * を対象とする。
	 * <p>既存データメンテナンス用</p>
	 * 
	 * @param _msg 該当タグを排除する前のコンテンツ本文
	 * @return 該当タグを排除したコンテンツ本文
	 */
	public static String filterTagClipper( String _msg ){
		String msg = _msg;
		if( StringUtils.isEmpty(msg) )
			return msg;

		Matcher m1 = Pattern.compile("<div class=\"cntrbtTxt\">(.*)</div>", Pattern.DOTALL).matcher(msg);
		if(m1.find())
			msg = m1.group(1);
		
		Matcher m2 = Pattern.compile("&lt;div class=&quot;cntrbtTxt&quot;&gt;(.*)&lt;/div&gt;", Pattern.DOTALL).matcher(msg);
		if(m2.find())
			msg = m2.group(1);
		
		Matcher m3 = Pattern.compile("&lt;div class=\"cntrbtTxt\"&gt;(.*)&lt;/div&gt;", Pattern.DOTALL).matcher(msg);
		if(m3.find())
			msg = m3.group(1);
		
		return msg;
	}



}

