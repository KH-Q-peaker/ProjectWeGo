package org.zerock.wego.mapper;

import java.util.concurrent.LinkedBlockingDeque;

import org.apache.ibatis.annotations.Param;
import org.zerock.wego.domain.CommentDTO;
import org.zerock.wego.domain.CommentVO;
import org.zerock.wego.domain.Target;

public interface CommentMapper {


	// 특정 글 댓글 총 개수 
	public abstract int selectCommentCountByTarget(Target cri);
	
	
	// 특정 글 댓글 offset 조회 (5개씩) 
	public abstract LinkedBlockingDeque<CommentVO> selectCommentsOffsetByTarget(Target cri);

	
	// 댓글 코드로 댓글 조회 
	public abstract CommentVO selectCommentByCommentId(@Param("commentId")Long commentId);
	
	
	// 특정 댓글 멘션 여부 조회 
	public abstract Integer selectMentionsByCommentId(@Param("commentId")Long commentId);
	
	// 특정 회원 댓글 조회 
	//	public abstract LinkedBlockingDeque<CommentVO> selectCommentByUser(@Param("userId") Long userId);
	
	
	// 댓글 코드로 댓글 삭제 
	public abstract Integer deleteCommentByCommentId(@Param("commentId")Long commentId);
	
	// 특정 유저 댓글 삭제 
	//	public abstract Integer deleteCommentByUserId(@Param("userId")Long userId);
	
	
	
	
	// 댓글 작성 
	public abstract Integer insertComment(CommentDTO comment);
	
	// 멘션 작성 
	public abstract Integer insertMention(CommentDTO comment);
	
	
	// 댓글 수정 
	public abstract Integer updateComment(CommentDTO comment);
	
	
}// end class
