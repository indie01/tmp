/**
 *
 */
package com.kickmogu.yodobashi.community.resource.domain;

import java.util.List;

import com.kickmogu.yodobashi.community.resource.domain.constants.ContentsStatus;
import com.kickmogu.yodobashi.community.resource.domain.constants.EditorVersions;

/**
 * Wysiwygエディタで編集可能なコンテンツです。
 * @author kamiike
 *
 */
public interface TextEditableContents {
	
	public String getContentId();
	
	public CommunityUserDO getCommunityUser();
	
	public ProductDO getProduct();
	/**
	 * ステータスを返します。
	 * @return ステータス
	 */
	public ContentsStatus getStatus();

	/**
	 * ステータスを設定します。
	 * @param status ステータス
	 */
	public void setStatus(ContentsStatus status);

	/**
	 * テキストエディタで編集可能な文章を返します。
	 * @return テキストエディタで編集可能な文章
	 */
	public String getTextEditableText();


	/**
	 * テキストエディタで編集可能な文章を設定します。
	 * @param text テキストエディタで編集可能な文章
	 */
	public void setTextEditableText(String text);
	
	/**
	 * エディターのバージョンを返却する
	 * @return
	 */
	public EditorVersions getEditorVersion();
	
	/**
	 * エディターのバージョンを設定する。
	 * @param version
	 */
	public void setEditorVersion(EditorVersions version);
	
	public List<ImageHeaderDO> getImageHeaders();
	
	public void setImageHeaders(List<ImageHeaderDO> imageHeaders);
	
	public List<SaveImageDO> getSaveImages();
	
	public void setSaveImages(List<SaveImageDO> saveImages);
	
	public List<String> getUploadImageIds();
	
	public void setUploadImageIds(List<String> uploadImageIds);
	

}
