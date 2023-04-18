<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%> <%@ taglib prefix="c"
uri="http://java.sun.com/jsp/jstl/core"%> <%@ taglib prefix="fmt"
uri="http://java.sun.com/jsp/jstl/fmt"%> <%@ taglib prefix="fn"
uri="http://java.sun.com/jsp/jstl/functions"%>

<jsp:include page="../common/report.jsp"></jsp:include><%-- 신고 --%>

<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <title>Wego 유저페이지</title>
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <link
      rel="shortcut icon"
      href="/resources/ico/favicon.ico"
      type="image/x-icon"
    />
    <link rel="icon" href="/resources/ico/favicon.ico" type="image/x-icon" />
    <link rel="stylesheet" href="/resources/css/header.css" />
    <link rel="stylesheet" href="/resources/css/footer.css" />
    <link rel="stylesheet" href="/resources/css/usercomment.css" />

    <link rel="stylesheet" href="/resources/css/userpageindex.css" />
    <link rel="stylesheet" href="/resources/css/userposts.css" />
    <link rel="stylesheet" href="/resources/css/top.css" />

    <script src="/resources/js/header.js" defer></script>
    <script src="/resources/js/top.js" defer></script>
    <script src="/resources/js/userpage.js" defer></script>

    <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.6.3/jquery.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery-migrate/3.4.0/jquery-migrate.min.js"></script>
  </head>
  <body>
    <div class="total-wrap">
      <!-- hearder start -->
      <%@include file="/WEB-INF/views/common/header.jsp"%>
      <!-- hearder end -->
      <!-- main start -->
      <section>
        <!-- Contents -->
        <div class="mypage-container">
          <!--  프로필 메인  -->
          <div class="profile">
            <%--<c:forEach items="${getUserInfoList}" var="userProfile">-->
              <!--<div class="profile-image">${userProfile.userPic}</div>--%>
              <img
                class="profile-image"
                src="${getUserInfoList.userPic}"
                alt="img"
              />
              <!--  사진  옆라인 -->
              <div class="name-badge-setting">
                <div class="div-name-setting" id="user-info">
                  <span class="nickname"
                    >&#127795; ${getUserInfoList.nickname} 님 &#127795;
                    <input type="hidden" id="userId" value="${userId}" />
                  </span>
                  <span class="sirenbar"
                    ><input
                      type="button"
                      class="report"
                      name="reportuser"
                      value="신고&#128680;"
                    />
                  </span>
                </div>
                <!--뱃지모음라인===== -->
                <div class="badge-collection">
                  <div class="badge-header">
                  <a class="badge-setting" href="/badge/${getUserInfoList.userId}"
                        >
                    <span>Badge Collection</span></a>
                    <span>
                      <a class="badge-setting" href="/badge/${getUserInfoList.userId}"
                        ><img src="/resources/img/badgeset.png" alt=""
                      /></a>
                    </span>
                  </div>

                  <!--적용 -->
                  <div class="badge-item1" id="1003">
                    <div class="sanBadge"></div>
                  </div>
                  <div class="badge-item2" id="1">
                    <div class="sanBadge"></div>
                  </div>
                  <div class="badge-item3" id="4">
                    <div class="sanBadge"></div>
                  </div>
                  <div class="badge-item4" id="5">
                    <div class="sanBadge"></div>
                  </div>
                  <div class="badge-item5" id="93">
                    <div class="sanBadge"></div>
                  </div>
                </div>
              </div>
            <%--</c:forEach>--%>
          </div>
          <!-- 프로필 아래  네비  -->
          <div id="content-section">
            <ul class="content-header-menu">
              <li class="content-header-menu-item" id="item-point">
                <input
                  id="content-link-written"
                  type="button"
                  class="profileMenu"
                  value="작성 글"
                  onclick="userPosts()"
                />
              </li>
              <li class="content-header-menu-item">
                <input
                  id="content-link-comment"
                  class="profileMenu"
                  type="button"
                  value="작성 댓글"
                  onclick="userComments()"
                />
              </li>
            </ul>
          </div>
          <!-- 내용물  -->
          <div id="module"></div>
        </div>
      </section>
      <!-- main end -->
    </div>
    <!-- total-wrap end -->
    <a href="#" class="scrollToTop">
      <img src="/resources/svg/top.svg" />
    </a>
    <!-- Footer Start -->
    <%@include file="/WEB-INF/views/common/footer.jsp"%>
    <!-- Footer End -->
  </body>
</html>
