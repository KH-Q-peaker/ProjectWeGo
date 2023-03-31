
function toggleBtn(inputElem, buttonElem) {
	
  if (inputElem.val() !== '') {
    buttonElem.prop('disabled', false).css({
      'color': 'white',
      'background-color': 'rgb(78, 197, 61)',
    });
  } else {
    buttonElem.prop('disabled', true).css({
      'color': 'gray',
      'background': 'buttonface'
    });
  }
}

$(() => { /* 새 댓글 post 전송  */
	
	$('textarea').off('keydow').on( 'keydown', function (){
   		$(this).css('height', 'auto');
    	$(this).height(this.scrollHeight);
  	});
  
	/* 입력내용에 따라 등록버튼 활성/비활성  */
	$("#contents").off('input').on('input', function(){
		
		toggleBtn($(this), $(this).next());
	});
	
	/* 취소 클릭시 내용 삭제 + 버튼 비활성 */
	$("#cmtwrite").children(".cancle").off('click').on('click', function(){
		$(this).prev().prev().val('');
		$(this).prev().prop('disabled', true).css({
				'color' : 'gray',
				'background' : 'buttonface' 
		});
	});
	
	
	$(".ncmt").off('click').on('click', function(){
		
		$.ajax({
			url : "/comment/register",
			type : "POST",
			data : 
			{
				targetGb : cri.targetGb,
				targetCd : cri.targetCd,
//				userId : userId, /*수정해야 됨 */
				contents : $(this).prev().val()
			},
			
			success : function(){
//				setMessage("✒️댓글이 등록되었습니다.");
//		 		showModal();
//		 		setTimeout(hideModal, 450);
				setTimeout(()=>{location.reload()}, 0);
			},
			error : function(){
			 	$(".mdcontainer").hide('fast');
				$(".modalbackground").remove();
		 		setMessage("⚠️ 댓글 등록 실패."); // 이거 고쳐ㅕㅕㅕㅕㅕㅕㅕㅕㅕ
		 		showModal();
		 		setTimeout(hideModal, 500);
			}
		});
	});
});


$(() => { /* 답글 관련 */
	$(".mentionwrite").hide();
	
  /* 답글 버튼 클릭시 멘션 작성 창 on */
  	$(".mentionbtn").off('click').on('click', function () {
		
		/* 다른 멘션창 모두 닫은 후 대상만 on  */
		$(".mentionwrite").hide('fast');
		$(".mcontents").val('');
		$(".mentionbtn").val("↪ ︎답글");
		$(".men").prop('disabled', true).css({
				'color' : 'gray',
				'background' : 'buttonface' 
		});
		
    	if ($(this).parent().next().css("display") == "none") {
	    	
			$(this).val("☓ 닫기");
	    	$(this).parent().next().show('normal');
    	}
  	});
  	/* 취소 클릭 시 멘션 작성 창 off  */
  	$(".mentionwrite").children(".cancle").off('click').on('click', function(){
		
		$(".mcontents").val('').height('80px');
		$(this).parent(".mentionwrite").hide('fast');
		$(".mentionbtn").attr('value', "↪ ︎답글");
	});
	
	/* 입력내용에 따라 등록버튼 활성/비활성  */
	$(".mcontents").off('input').on('input', function(){
		
		toggleBtn($(this), $(this).next());
	});
	
	/* 등록버튼 클릭 시 멘션 등록 post전송   */
	$(".men").off('click').on('click', function(){
		
		$.ajax({
			url : "/comment/reply",
			type : "POST",
			data : 
			{
				targetGb :cri.targetGb,
				targetCd : cri.targetCd,
				mentionId : $(this).siblings("#mentionId").val(),
//				userId : 160, /*수정해야 됨 */
				contents : $(this).prev().val()
			},
			
			success : function(){
				setMessage("️📝 답글이 등록되었습니다.");
		 		showModal();
		 		setTimeout(hideModal, 500);
				setTimeout(()=>{location.reload()}, 600);
			},
			error : function(){
		 		setMessage("⚠️ 답글 등록 실패."); // 이거 고쳐ㅕㅕㅕㅕㅕㅕㅕㅕㅕ
		 		showModal();
		 		setTimeout(hideModal, 700);
			}
		});
	});
});


$(() => { /* 수정 관련 */
	
	/* 댓글 수정  */
	$(".modifycmt").off('click').on('click', function() {
		
		/* 댓글 수정중일 때 다른 수정버튼 비활성화 */
		$(".modifycmt").prop('disabled', true);
		
		let commentId = $(this).siblings('#commentId').val();
		let target = $(this).parent().next();
		let contents = target.text();
	
		/* 댓글 수정 폼 on */
		target.siblings().not('.cmtuser').not('.cmtuserPic').toggle('fast');
  		
  		let modifying = $("<textarea></textarea>").addClass("comment update")
  						.height(target.height() + 20 +'px')
  						.off('keydown').on('keydown', function(){
					   		$(this).css('height', 'auto');
    						$(this).height(this.scrollHeight - 30 + 'px');
  						});
  		modifying.val(contents);
  		target.replaceWith(modifying);

  		
		/* 댓글 수정 폼 off  */
		$("input[name='updatecls']").off('click').on('click', function() {

			/* 수정 중 취소 눌렀을 때 다른 수정버튼 재활성화  */
			$(".modifycmt").prop('disabled', false);
			
			modifying.replaceWith(target);
			target.siblings().not('.cmtuser').not('.cmtuserPic').toggle('fast');
		});
	
		/* 수정 post전송  */
		$("input[name='updatecmt']").off('click').on('click', function() {
				
			$.ajax({
				url : "/comment",
				type : "PATCH",
				data : 
				JSON.stringify({
					commentId : commentId,
					contents : $(this).parent().siblings(".update").val()
				}),
				contentType :'application/JSON',
				success : function(data){
					setMessage("✏️ 댓글이 수정되었습니다.");
		 			showModal();
		 			setTimeout(hideModal, 500);
					modifying.replaceWith(target.text(modifying.val()));
					target.siblings().not('.cmtuser').not('.cmtuserPic').toggle('fast');
					$(".modifycmt").prop('disabled', false);
				},
				error : function(){
		 			setMessage("⚠️ 수정 실패."); // 이거 고쳐ㅕㅕㅕㅕㅕㅕㅕㅕㅕ
		 			showModal();
		 			setTimeout(hideModal, 500);
				}	
			});
		});
	});
});

// =====================================================================================


