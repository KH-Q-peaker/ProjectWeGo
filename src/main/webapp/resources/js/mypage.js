// const mountains = [];

let imgPath; // 업로드 이미지 임시 저장 변수
let isReadyUpload = false; // 파일 업로드 가능여부

//닉네임 변경
var nickbox = document.querySelector(".nickname-box");
var nameset = document.querySelector(".name-setting");
nameset.onclick = function () {
  nickbox.classList.toggle("active");
};
var closebutton2 = document.querySelector(".closebutton2");
closebutton2.onclick = function () {
  nickbox.className = "nickname-box";
};

//파일첨부 text 표시 관련
var document_file = document.querySelector("#document_file");
var fileText = document.querySelector("#fileText");
var profileimagebutton = document.querySelector(".profile-image-button");
profileimagebutton.addEventListener('click', function(){
  fileText.innerHTML="여기로 이미지를 드래그하거나 <br> 파일을 업로드 하세요. (최대 20MB)";
  $('#document_file').on('change', function(e){
    fileText.innerHTML="";
  });
});


// 이미지 추가 버튼 클릭 이벤트
$$(".profile-image-button").addEventListener("click", () => {
  $('#document_file').on('change', function(event){
      const selectedFile = document.getElementById('document_file').files[0];
      console.log("fileName:{}",event.target.files[0].name)
      selectedFile.type = "file";
      selectedFile.accept = ".jpg, .jpeg, .png";
    
        // 업로드 파일 용량 체크
        var check = isFileMaxSize(event);
    
        // 파일형식 체크
        var check2 =isRightFile(event);
        console.log("check:{}", check);

        // 위 조건을 모두 통과할 경우
        isReadyUpload = true;
        if(! (check && check2)){
          console.log("check : fileName:{}",event.target.files[0].name)
          processFile(event.target.files[0]);
          var fileName = event.target.files[0].name;

          if (fileName) {
            $(event.target).parent().attr('data-message', fileName);
          }
        }
        
       
          var formData = new FormData();
		  formData.append("part",document.getElementById('document_file').files[0]);
		  formData.append("user_id",document.getElementById('user_id').value);
		   $('#partButton').on('click', function(event){
		  $.ajax({
		    url: '/file/upload',
		      processData: false,
		      contentType: false,
		      data: formData,
		      type: 'POST',
		      success: function(result){
				//로딩 함수
				_showPage();
				//이미지가 로딩될때까지 기다림
    			
    			setTimeout(function(){
    			//전송 성공시, 메인페이지로 이동
				const user_id = document.getElementById('user_id').value;
			     location.href = '/mypage/mypage?user_id=' + user_id;
				}, 1000);
			     //});
		      }
		  });
		});
        
        
        
  });
  $$(".add-profile-image").style.display = "block";
});



// 이미지 추가 취소 버튼 클릭 이벤트
$$(".add-profile-image .cancel").addEventListener("click", () => {
  $$(".add-profile-image").style.display = "none";
  window.location.reload();
});


const processFile = (file) => {
  const reader = new FileReader();

  reader.readAsDataURL(file);

  reader.onload = function () {
    $$(".drag-and-drop").innerHTML = `<p>${file.name}</p>`;

    imgPath = `<img name="ImagePath" src="${reader.result}" value="${reader.result}" alt="프로필 이미지"></img>
    <input type="hidden" name="imagePath" id="imagePath" value="${reader.result}">`;
  };
};

// 업로드 파일 용량 체크
const isFileMaxSize = (event) => {
  console.log("isFileMaxSize() invoked");
  if (event.target.files[0].size > 20971520) {
    $$(".drag-and-drop").innerHTML = 
    "<p>최대 업로드 용량은 20MB입니다.<br>"
    +"현재 파일의 용량은 ${Math.floor((event.target.files[0].size / 1048576) * 10) / 10}입니다."
    +"</p>";
    console.log("event.target.files[0].size:{}",event.target.files[0].size);
    isReadyUpload = false;
    return true;
  } else {
    return false;
  }
};

// 파일형식 체크
const isRightFile = (event) => {
  console.log("isRightFile() invoked");
  if (
    event.target.files[0].type !== "image/jpeg" &&
    event.target.files[0].type !== "image/png" &&
    event.target.files[0].type !== "image/jpg"
  ) {
    $$(".drag-and-drop").innerHTML = `
    <p>업로드 가능한 파일 형식은<br>
    .jpg, .jpeg, .png입니다.
    </p>`;
    console.log("event.target.files[0].type:{}",event.target.files[0].type);
    isReadyUpload = false;
    return true;
  } else {
    return false;
  }
};



//쿼리스트링 가져오기(user_id만, 여러개일시 getAll)
var urlSearch = new URLSearchParams(location.search);
var user_id = urlSearch.get('user_id');
console.log("user_id:{}",user_id);

//쿼리스트링 가져오기(currPage,amount})
var aCurrPage = urlSearch.get('aCurrPage');
console.log("aCurrPage:{}",aCurrPage);
var aAmount = urlSearch.get('aAmount');
console.log("aAmount:{}",aAmount);

var pCurrPage = urlSearch.get('pCurrPage');
console.log("pCurrPage:{}",pCurrPage);
var pAmount = urlSearch.get('pAmount');
console.log("pAmount:{}",pAmount);


//메인 페이지 로드
window.onload = function(){
	aCurrPage = 1;
	aAmount = 5;
	pCurrPage = 1;
	pAmount = 5;
	$.ajax({
        type: 'get',
        url: '/mypage/myclimb',
        data:{"aCurrPage":aCurrPage,"aAmount":aAmount,"pCurrPage":pCurrPage,"pAmount":pAmount,"user_id":user_id},
        success: function(data){
            $("#module").load("/mypage/myclimb?aCurrPage="+aCurrPage + "&aAmount="+aAmount+ "&pCurrPage="+pCurrPage + "&pAmount="+pAmount+"&user_id="+ user_id);
        }
   });
	
};



$('#climb').click(function(){
	aCurrPage = 1;
	aAmount = 5;
	pCurrPage = 1;
	pAmount = 5;
   $.ajax({
        type: 'get',
        url: '/mypage/myclimb',
        data:{"aCurrPage":aCurrPage,"aAmount":aAmount,"pCurrPage":pCurrPage,"pAmount":pAmount,"user_id":user_id},
        success: function(data){
            $("#module").load("/mypage/myclimb?aCurrPage="+aCurrPage + "&aAmount="+aAmount+ "&pCurrPage="+pCurrPage + "&pAmount="+pAmount+"&user_id="+ user_id);
        }
   });
    return false; //<- 이 문장으로 새로고침(reload)이 방지됨
});


$('#info').click(function(){
	$.ajax({
		async : true,
        type: 'get',
        url: '/mypage/myinfo',
        data:{"user_id":user_id},
        success: function(data){
        	$("#module").load("/mypage/myinfo?user_id=" + user_id);
 		}
    });
});



