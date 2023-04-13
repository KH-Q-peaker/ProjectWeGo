const $$ = (selector) => document.querySelector(selector);

// Header Event init
addEventListener("click", (e) => {
  // 회원 메뉴바가 열린상태에서 다른 곳을 클릭하면 메뉴바 숨기기
  if (e.target.id !== "userImg") {
    $$("#userImg").classList.remove("toggle");
    $$(".menubar").style.display = "none";
  } // if

  // 검색바에 포커스가 없을 때 입력테스트 삭제버튼 숨기기
  // 글 작성 폼에는 검색바가 없으므로 if로 null 체크
  if (e.target.id !== "search" && $$(".search-bar .cancel") != null) {
    $$(".search-bar .cancel").style.display = "none";
  } // if
});

// 회원 이미지 클릭 시 메뉴바 표시
$$("#userImg")?.addEventListener("click", () => {
  if ($$("#userImg").classList.toggle("toggle")) {
    $$(".menubar").style.display = "block";
  } else {
    $$(".menubar").style.display = "none";
  }
});

// header에서 검색바를 제거하는 함수
const removeSearch = () => selector(".search-bar").remove();



$.ajax({
  type: "GET",
  url: "/auth/json/session",
  error: function () {
      console.log("noUser");

      $(".user #userImg").css("display","none");
      $(".user .login-btn").css("display","block");
  },

  success: function (auth) {
      authJson = auth;

      console.log(authJson);

      $(".user #userImg").css("display","block");
      $(".user .login-btn").css("display","none");
  }
});