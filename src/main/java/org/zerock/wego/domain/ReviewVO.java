package org.zerock.wego.domain;

import java.sql.Timestamp;

import lombok.Value;


@Value
public class ReviewVO {

	private Long sanReviewId;
	private String sanName;
	private Long userId;
	private String nickname;
	private String userPic;
	private Timestamp createdDt;
	private Timestamp modifiedDt;
	private String title;
	private String contents;
	private Long likeCnt;
	private Long reportCnt;
	
}// end class
