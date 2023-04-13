package org.zerock.wego.domain.mypage;

import lombok.Value;

@Value
public class WegoUserTbVO {
	private String user_id;
	private String nickname;
	private String address;
	private String san_range;
	private String san_taste;
	private String user_pic;
	private String social_id;
	private String filename;
}//end class
