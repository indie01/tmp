package com.kickmogu.yodobashi.community.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

import com.kickmogu.lib.solr.SolrConstants;
import com.kickmogu.yodobashi.community.common.test.YcComJUnit4ClassRunner;
import com.kickmogu.yodobashi.community.resource.domain.CommentDO;
import com.kickmogu.yodobashi.community.resource.domain.ImageHeaderDO;
import com.kickmogu.yodobashi.community.resource.domain.ProductDO;
import com.kickmogu.yodobashi.community.resource.domain.SearchResult;
import com.kickmogu.yodobashi.community.resource.domain.constants.ActionHistoryType;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommentTargetType;
import com.kickmogu.yodobashi.community.resource.domain.constants.InformationType;
import com.kickmogu.yodobashi.community.resource.domain.constants.PostContentType;

/**
 * 違反報告サービスのテストクラスです。
 *
 * @author hirabayashi
 *
 */
@RunWith(YcComJUnit4ClassRunner.class)
@ContextConfiguration("/jcclientContext.xml")
public class CommentServiceTest extends DataSetTest {

	/**
	 * データを初期化します。
	 */
	protected void initialize() {
		super.initialize();
	}

	/**
	 * 全ての情報を削除して、初期化します。
	 */
	@Before
	public void setup() {
		initialize();
	}

	/**
	 * 画像コメントを投稿・検証します。
	 */
	@Test
	public void testSaveComment() {
		// 画像を取得します。
		SearchResult<ImageHeaderDO> imageHeaders = findImageHeaderBySolr(product);
		// コメントを投稿・検証します。
		CommentDO comment = null;
		CommentDO saveComment = createComment(imageHeaders.getDocuments().get(0), comment, "画像コメントです");
		// コメントのアクションヒストリーを検証します。
		checkActionHistory(saveComment.getCommunityUser(), saveComment, ActionHistoryType.USER_IMAGE_COMMENT);
		// コメントを編集・検証します。
		createComment(imageHeaders.getDocuments().get(0), saveComment, "画像コメント編集です");
		// コメントを削除・検証します。
		deleteComment(saveComment);
		// アクションヒストリーを検証します。
		checkDeleteActionHistory(ActionHistoryType.USER_IMAGE_COMMENT, commentUser);
		// お知らせを検証します。
		checkDeleteInformation(review, InformationType.IMAGE_COMMENT_ADD);
	}

	/**
	 * 画像を取得します。
	 * @return
	 */
	private SearchResult<ImageHeaderDO> findImageHeaderBySolr(ProductDO product) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("productId_s:");
		buffer.append(product.getSku());
		buffer.append(" AND postContentType_s:");
		buffer.append(PostContentType.IMAGE_SET.getCode());
		SolrQuery query = new SolrQuery(buffer.toString()).setRows(SolrConstants.QUERY_ROW_LIMIT).
				setSortField("imageSetIndex_i", ORDER.asc);
		SearchResult<ImageHeaderDO> imageHeaders = new SearchResult<ImageHeaderDO>(
				solrOperations.findByQuery(query, ImageHeaderDO.class, imageHeaderPath));

		return imageHeaders;
	}

	/**
	 * コメントの登録・検証を行います。
	 *
	 * @param review
	 */
	private CommentDO createComment(ImageHeaderDO imageHeader, CommentDO comment, String commentText) {
		if(comment==null) {
			comment = new CommentDO();
		}
		comment.setCommunityUser(commentUser);
		comment.setTargetType(CommentTargetType.IMAGE);
		comment.setImageHeader(imageHeader);
		comment.setCommentBody(commentText);
		CommentDO saveComment = commentService.saveComment(comment);

		// コメントを取得します。
		CommentDO commentByHBase = hBaseOperations.load(CommentDO.class,
				saveComment.getCommentId(), commentPath);
		CommentDO commentBySolr = solrOperations.load(CommentDO.class,
				saveComment.getCommentId(), commentPath);

		// コメントを検証します。
		checkComment(comment, commentByHBase);
		checkComment(comment, commentBySolr);
		assertEquals(commentBySolr.getCommentId(), commentByHBase.getCommentId());
		return saveComment;
	}

	/**
	 * コメントの検証を行います。
	 *
	 * @param comment
	 */
	private void checkComment(CommentDO comment, CommentDO checkComment) {
		assertEquals(comment.getCommunityUser().getCommunityUserId(),
				checkComment.getCommunityUser().getCommunityUserId());
		assertEquals(comment.getTargetType(), checkComment.getTargetType());
		assertEquals(comment.getCommentBody(), checkComment.getCommentBody());
		if(CommentTargetType.REVIEW.equals(comment.getTargetType())) {
			assertNotNull(comment.getReview().getReviewId());
			assertEquals(comment.getReview().getReviewId(),
					checkComment.getReview().getReviewId());
		}
		if(CommentTargetType.QUESTION_ANSWER.equals(comment.getTargetType())) {
			assertNotNull(comment.getQuestionAnswer().getQuestionAnswerId());
			assertNotNull(comment.getQuestionId());
			assertEquals(comment.getQuestionAnswer().getQuestionAnswerId(),
					checkComment.getQuestionAnswer().getQuestionAnswerId());
			assertEquals(comment.getQuestionId(), checkComment.getQuestionId());
		}
		if(CommentTargetType.IMAGE.equals(comment.getTargetType())) {
			assertNotNull(comment.getImageHeader().getImageId());
			assertEquals(comment.getImageHeader().getImageId(),
					checkComment.getImageHeader().getImageId());
		}
		assertNotNull(checkComment.getPostDate());
		assertEquals(null, checkComment.getDeleteDate());
		assertEquals(comment.getSpamReports().size(), checkComment.getSpamReports().size());
		assertEquals(comment.getInformations().size(), checkComment.getInformations().size());
		assertEquals(comment.getActionHistorys().size(), checkComment.getActionHistorys().size());
	}

	/**
	 * コメントの削除・検証をします。
	 * @param comment
	 */
	private void deleteComment(CommentDO comment) {
		requestScopeDao.initialize(comment.getCommunityUser(), null);
		commentService.deleteComment(comment.getCommentId());
		requestScopeDao.destroy();
		// コメントを取得します。
		CommentDO commentByHBase = hBaseOperations.load(CommentDO.class,
				comment.getCommentId(), commentPath);
		CommentDO commentBySolr = solrOperations.load(CommentDO.class,
				comment.getCommentId(), commentPath);
		assertNotNull(commentByHBase);
		checkDeleteComment(commentByHBase);
		checkDeleteComment(commentBySolr);
	}

	/**
	 * コメントの検証をします。
	 */
	private void checkDeleteComment(CommentDO comment) {
		assertNotNull(comment.getDeleteDate());
		assertTrue(comment.isDeleted());
	}

}