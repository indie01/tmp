package com.kickmogu.yodobashi.community.tdc2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;

import com.google.common.collect.Lists;
import com.kickmogu.lib.core.exception.CommonSystemException;
import com.kickmogu.lib.hadoop.hbase.HBaseContainer;
import com.kickmogu.lib.hadoop.hbase.meta.HBaseTableMeta;
import com.kickmogu.yodobashi.community.resource.domain.ActionHistoryDO;
import com.kickmogu.yodobashi.community.resource.domain.CommentDO;
import com.kickmogu.yodobashi.community.resource.domain.CommunityUserDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageDO;
import com.kickmogu.yodobashi.community.resource.domain.InformationDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionAnswerDO;
import com.kickmogu.yodobashi.community.resource.domain.QuestionDO;
import com.kickmogu.yodobashi.community.resource.domain.ReviewDO;

public class TestId {
	
	public static  String DIRECTORY = "\\\\192.168.101.101\\public\\data\\testData";
	static {
		if (System.getProperty("testData.dir") != null) {
			DIRECTORY = System.getProperty("testData.dir");
		}
	}
	
	static class CommunityUserId extends Target<CommunityUserDO> {
		public static void hoge() {writeIdList(CommunityUserId.class, 50000);}

	}

	static class ReviewId extends Target<ReviewDO> {
		public static void hoge() {writeIdList(ReviewId.class, 500000);}
	}

	static class QuestionId extends Target<QuestionDO> {
		public static void hoge() {writeIdList(QuestionId.class, 500000);}
	}
	
	static class QuestionAnswerId extends Target<QuestionAnswerDO> {
		public static void hoge() {writeIdList(QuestionAnswerId.class, 500000*2);}
	}
	
	static class CommunityUserImageId extends Target<ImageDO> {
		public static void main(String[] a) {writeIdList(CommunityUserImageId.class, 50000);}
	}
	
	static class CommunityUserThumbnailImageId extends Target<ImageDO> {
		public static void main(String[] a) {writeIdList(CommunityUserThumbnailImageId.class, 50000);}
	}
	
	static class ReviewImageId extends Target<ImageDO> {
		public static void hoge() {writeIdList(ReviewImageId.class, 100000);}
	}

	static class ReviewThumbnailImageId extends Target<ImageDO> {
		public static void hoge() {writeIdList(ReviewThumbnailImageId.class, 100000);}
	}
	
	static class QuestionImageId extends Target<ImageDO> {
		public static void hoge() {writeIdList(QuestionImageId.class, 100000);}
	}
	
	static class QuestionThumbnailImageId extends Target<ImageDO> {
		public static void hoge() {writeIdList(QuestionThumbnailImageId.class, 100000);}
	}
	
	static class QuestionAnswerImageId extends Target<ImageDO> {
		public static void hoge() {writeIdList(QuestionAnswerImageId.class, 200000);}
	}
	
	static class QuestionAnswerThumbnailImageId extends Target<ImageDO> {
		public static void hoge() {writeIdList(QuestionAnswerThumbnailImageId.class, 200000);}
	}
	
	static class ProductSetImageId extends Target<ImageDO> {
		public static void hoge() {writeIdList(ProductSetImageId.class, 200000);}
	}
	
	static class ProductSetThumbnailImageId extends Target<ImageDO> {
		public static void hoge() {writeIdList(ProductSetThumbnailImageId.class, 200000);}
	}
	
	static class ImageSetId extends Target<ImageDO> {
		public static void hoge() {writeIdList(ImageSetId.class, 50000);}
	}
	
	static class ReviewCommentId extends Target<CommentDO> {
		public static void hoge() {writeIdList(ReviewCommentId.class, 1000000);}
	}

	static class QuestionAnswerCommentId extends Target<CommentDO> {
		public static void hoge() {writeIdList(QuestionAnswerCommentId.class, 4000000);}
	}

	static class ProductSetImageCommentId extends Target<CommentDO> {
		public static void hoge() {writeIdList(ProductSetImageCommentId.class, 400000);}
	}
	
	static class UserReviewActionHistoryId extends Target<ActionHistoryDO> {
		public static void hoge() {writeIdList(UserReviewActionHistoryId.class, 500000);}
	}
	
	static class UserQuestionActionHistoryId extends Target<ActionHistoryDO> {
		public static void hoge() {writeIdList(UserQuestionActionHistoryId.class, 500000);}
	}
	
	/**
	 * 	USER_ANSWER("03", "質問に回答", ActionHistoryGroup.USER),
	USER_IMAGE("04", "画像を投稿", ActionHistoryGroup.USER),
	USER_REVIEW_COMMENT("05", "レビューにコメント", ActionHistoryGroup.USER),
	USER_ANSWER_COMMENT("06", "質問回答にコメント", ActionHistoryGroup.USER),
	USER_IMAGE_COMMENT("07", "画像にコメント", ActionHistoryGroup.USER),
	USER_FOLLOW_USER("08", "ユーザーをフォロー", ActionHistoryGroup.USER),
	USER_FOLLOW_PRODUCT("09", "商品をフォロー", ActionHistoryGroup.USER),
	USER_FOLLOW_QUESTION("10", "質問をフォロー", ActionHistoryGroup.USER),
	USER_PRODUCT_MASTER_RANK_CHANGE("11", "商品マスターランクイン", ActionHistoryGroup.USER),
	PRODUCT_REVIEW("01", "商品の新着レビュー", ActionHistoryGroup.PRODUCT),
	PRODUCT_QUESTION("02", "商品の新着質問", ActionHistoryGroup.PRODUCT),
	PRODUCT_ANSWER("03", "商品の新着回答", ActionHistoryGroup.PRODUCT),
	PRODUCT_IMAGE("04", "商品の新着画像", ActionHistoryGroup.PRODUCT),
	QUESTION_ANSWER("01", "質問の新着回答", ActionHistoryGroup.QUESTION),
	LIKE_REVIEW_50("01", "レビューにいいねが50回", ActionHistoryGroup.LIKE),
	LIKE_ANSWER_50("02", "質問回答にいいねが50回", ActionHistoryGroup.LIKE),
	LIKE_IMAGE_50("03", "画像にいいねが50回", ActionHistoryGroup.LIKE),
	 *
	 */
	static class UserAnswerActionHistoryId extends Target<ActionHistoryDO> {public static void hoge() {writeIdList(UserAnswerActionHistoryId.class, 1000000);}}
	static class UserImageActionHistoryId extends Target<ActionHistoryDO> {public static void hoge() {writeIdList(UserImageActionHistoryId.class, 50000);}}

	static class UserReviewCommentActionHistoryId extends Target<ActionHistoryDO> {public static void hoge() {writeIdList(UserReviewCommentActionHistoryId.class, 1000000);}}
	static class UserAnswerCommentHistoryId extends Target<ActionHistoryDO> {public static void hoge() {writeIdList(UserAnswerCommentHistoryId.class, 2000000);}}
	static class UserImageCommentActionHistoryId extends Target<ActionHistoryDO> {public static void hoge() {writeIdList(UserImageCommentActionHistoryId.class, 4000000);}}
	
	static class UserFollowUserActionHistoryId extends Target<ActionHistoryDO> {public static void hoge() {writeIdList(UserFollowUserActionHistoryId.class, 1000000);}}
	static class UserFollowProductActionHistoryId extends Target<ActionHistoryDO> {public static void hoge() {writeIdList(UserFollowProductActionHistoryId.class, 1000000);}}
	static class UserFollowQuestionActionHistoryId extends Target<ActionHistoryDO> {public static void hoge() {writeIdList(UserFollowQuestionActionHistoryId.class, 1000000);}}
	static class ProductReviewActionHistoryId extends Target<ActionHistoryDO> {public static void hoge() {writeIdList(ProductReviewActionHistoryId.class, 500000);}}
	static class ProductQuestionActionHistoryId extends Target<ActionHistoryDO> {public static void hoge() {writeIdList(ProductQuestionActionHistoryId.class, 500000);}}
	static class ProductAnswerActionHistoryId extends Target<ActionHistoryDO> {public static void hoge() {writeIdList(ProductAnswerActionHistoryId.class, 1000000);}}
	static class ProductImageActionHistoryId extends Target<ActionHistoryDO> {public static void hoge() {writeIdList(ProductAnswerActionHistoryId.class, 50000);}}
	static class QuestionAnswerActionHistoryId extends Target<ActionHistoryDO> {public static void hoge() {writeIdList(QuestionAnswerActionHistoryId.class, 1000000);}}
	
/**
 * 
 	PRODUCT_MASTER_RANK_CHANGE("1", "商品マスタ－ランクイン・順位変動"),
	REVIEW_COMMENT_ADD("2", "レビューにコメント追加"),
	REVIEW_LIKE_ADD("3", "レビューにいいね評価追加"),
	QUESTION_ANSWER_COMMENT_ADD("4", "QA回答にコメント追加"),
	QUESTION_ANSWER_LIKE_ADD("5", "QA回答にいいね評価追加"),
	IMAGE_COMMENT_ADD("6", "画像にコメント追加"),
	IMAGE_LIKE_ADD("7", "画像にいいね評価追加"),
	QUESTION_ANSWER_ADD("8", "QA質問に回答追加"),
	FOLLOW("9", "フォローされた場合"),
	POINT_REVIEW("10", "レビューポイントを獲得"),
	POINT_COMMUNITY("11", "コミュニティ貢献ポイントを獲得"),
	ACCOUNT_STOP("12", "アカウントを強制一時停止された"),
	WELCOME("13", "会員登録時"),
 *
 */
	static class ReviewCommentAddInformationId extends Target<InformationDO> {public static void hoge() {writeIdList(ReviewCommentAddInformationId.class, 1000000);}}
	static class ReviewLikeAddInformationId extends Target<InformationDO> {public static void hoge() {writeIdList(ReviewLikeAddInformationId.class, 1000000);}}
	static class QuestionAnswerCommentAddInformationId extends Target<InformationDO> {public static void hoge() {writeIdList(QuestionAnswerCommentAddInformationId.class, 2000000);}}
	static class QuestionAnswerLikeAddInformationId extends Target<InformationDO> {public static void hoge() {writeIdList(QuestionAnswerLikeAddInformationId.class, 2000000);}}
	static class ImageCommentAddInformationId extends Target<InformationDO> {public static void hoge() {writeIdList(ImageCommentAddInformationId.class, 400000);}}
	static class ImageLikeAddInformationId extends Target<InformationDO> {public static void main(String[] args) {writeIdList(ImageLikeAddInformationId.class, 2000000);}}
	static class QuestionAnswerAddInformationId extends Target<InformationDO> {public static void hoge() {writeIdList(QuestionAnswerAddInformationId.class, 1000000);}}
	static class FollowInformationId extends Target<InformationDO> {public static void hoge() {writeIdList(FollowInformationId.class, 100000);}}
	static class WelcomeInformationId extends Target<InformationDO> {public static void hoge() {writeIdList(WelcomeInformationId.class, 50000);}}

	static class NullId extends Target<Object> {

		public static void hoge() {}
		@SuppressWarnings("unchecked")
		@Override
		public List<String> read(int num) {
			return ListUtils.EMPTY_LIST;
		}
		
	}
	
	//////////////////////////////////////////////////////////////////////////
	
	@SuppressWarnings("rawtypes")
	public static <T extends TestId.Target> void writeIdList(Class<T> targetType, int num) {
		try {
			targetType.newInstance().write(num);
		} catch (Throwable e) {
			throw new CommonSystemException(e);
		} finally {
		//	System.exit(0);			
		}
	}
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T extends TestId.Target> List<String> readIdList(Class<T> targetType, int num) {
		try {
			return targetType.newInstance().read(num);
		} catch (Throwable th) {
			throw new CommonSystemException(th);
		}
	}
	
	abstract static class Target<T> {
		
		@Autowired @Qualifier("MySite")
		HBaseContainer hBaseContainer;
		
		public void write(int num) {
		//	if (new File(getFileName()).isFile()) return;
			
			FileWriter fileWriter =null;
			try {
				ApplicationContext context = DataCreator.getApplicationContext();
				context.getAutowireCapableBeanFactory().autowireBeanProperties(this,
								AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false);
				@SuppressWarnings("unchecked")
				Class<T> type = (Class<T>)((ParameterizedType)this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
				 fileWriter = new FileWriter(getFileName());
				HBaseTableMeta tableMeta = hBaseContainer.getMeta().getTableMeta(type);
				for (int i = 0 ; i < num ; i++) {
					fileWriter.write((String)tableMeta.getKeyMeta().generateKey(tableMeta.createObject())+"\n");
					fileWriter.flush();
				}
			} catch (Throwable th) {
				throw new CommonSystemException(this.getFileName()+" failed", th);
			} finally {
				IOUtils.closeQuietly(fileWriter);
			}
			
		}
		
		public List<String> read(int num) {
			List<String> result = Lists.newArrayList();
			BufferedReader reader = null;
			int i = 1;
			try {
				reader = new BufferedReader(new FileReader(getFileName()));
				for (; i <= num; i++) {
					result.add(reader.readLine().trim());					
				}
			} catch (Throwable e) {
				throw new CommonSystemException(this.getFileName()+" failed("+num+","+i+")", e);
			} finally {
				IOUtils.closeQuietly(reader);
			}
			return result;
		}
		
		public String getFileName() {
			return DIRECTORY + File.separator +  this.getClass().getSimpleName().replaceAll("FileWriter$", "") + ".dump";
		}
	}
	
	public static void main(String[] args) throws Throwable {
		System.out.println(TestId.class.getDeclaredClasses().length);
		for (Class<?> innerClass:TestId.class.getDeclaredClasses()) {
			System.out.println(innerClass.getSimpleName()+"<<");
			if (!Target.class.isAssignableFrom(innerClass)) continue;
			if (Modifier.isAbstract(innerClass.getModifiers())) continue;
			System.out.println(innerClass.getSimpleName()+">>");
//			System.out.println(innerClass.getDeclaredMethod("main", String[].class).getParameterTypes()[0]);
//			System.out.println(innerClass.getDeclaredMethod("main", String[].class));
			Method m = innerClass.getDeclaredMethod("hoge");
			m.setAccessible(true);
			m.invoke(null);
		}
		System.exit(0);
	}

}
