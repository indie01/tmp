/**
 *
 */
package com.kickmogu.yodobashi.community.resource.dao;

import com.kickmogu.yodobashi.community.resource.domain.RemoveContentsDO;
import com.kickmogu.yodobashi.community.resource.domain.constants.CommunityContentsType;


/**

 * @author kamiike
 *
 */
public interface RemoveContentsDao {

	public void save(RemoveContentsDO removeContents);
	
	public void updateRemoveContentsInIndex(String contentsId);
	
	public RemoveContentsDO getRemoveContents(CommunityContentsType type, String contentsId);
}
