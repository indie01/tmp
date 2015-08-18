package com.kickmogu.yodobashi.community.common.mapper;

import java.util.List;


/**
 * Beanをconvertするための手段を提供するインタフェース.
 * 
 * @author matsui
 */
public interface BeanMapperLogic {

	/**
	 * destinationClassクラスのインスタンスを生成し、sourceオブジェクトからの
	 * 対象プロパティをコピーして返します。
	 * 
	 * @param source コピー元オブジェクト
	 * @param destinationClass コピー先クラス
	 * @return 内部で生成した<T>型のインスタンス
	 */
	public <T> T map(Object source, Class<T> destinationClass);
	
	/**
	 * 引数のsourceのList実装クラスのインスタンスを生成し、sourceオブジェクトからの
	 * 対象プロパティをコピーして返します。
	 * 
	 * @param source コピー元オブジェクトのList
	 * @param destinationClass コピー先Listの要素のクラス
	 * @return
	 */
	public <T> List<T> map(List<?> source, Class<T> destinationClass);	
}
