/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao;

import com.kickmogu.yodobashi.community.performance.PerformanceTest;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Frequency;
import com.kickmogu.yodobashi.community.performance.PerformanceTest.Type;
import com.kickmogu.yodobashi.community.resource.domain.constants.ImageDeleteResult;
import com.kickmogu.yodobashi.community.resource.domain.constants.ImageUploadResult;



/**
 * 画像キャッシュの DAO です。
 * @author kamiike
 *
 */
public interface ImageCacheDao {
	/**
	 * 指定した画像をアップロードします。
	 * @param data 画像データ
	 * @param remoteTargetDirectory アップロードディレクトリ
	 * @param remoteFileName アップロード先ファイル名
	 * @return 実行結果
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.NONE, frequencyComment="画像アップロードは稀。ssh,Akamai処理はテスト対象外")
	public ImageUploadResult upload(byte[] data, String remoteTargetDirectory, final String remoteFileName);

	/**
	 * 指定した画像を削除します。
	 * @param remoteTargetDirectory アップロードディレクトリ
	 * @param remoteFileName アップロード先ファイル名
	 * @return 実行結果
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.NONE, frequencyComment="画像削除は稀。ssh,Akamai処理はテスト対象外")
	public ImageDeleteResult delete(String remoteTargetDirectory, final String remoteFileName);

	/**
	 * 指定したパスのキャッシュをクリアします。
	 * @param path パス
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.NONE, frequencyComment="画像削除は稀。ssh,Akamai処理はテスト対象外")
	public void clearCache(String path);

	/**
	 * 指定したパスのキャッシュをクリアします。
	 * @param paths パスリスト
	 */
	@PerformanceTest(type=Type.UPDATE, frequency=Frequency.NONE, frequencyComment="画像削除は稀。ssh,Akamai処理はテスト対象外")
	public void clearCaches(String[] paths);

}
