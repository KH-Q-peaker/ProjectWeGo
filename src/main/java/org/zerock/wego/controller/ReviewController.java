package org.zerock.wego.controller;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.zerock.wego.domain.common.CommentViewVO;
import org.zerock.wego.domain.common.FavoriteDTO;
import org.zerock.wego.domain.common.FileDTO;
import org.zerock.wego.domain.common.FileVO;
import org.zerock.wego.domain.common.PageInfo;
import org.zerock.wego.domain.common.UserVO;
import org.zerock.wego.domain.review.ReviewDTO;
import org.zerock.wego.domain.review.ReviewViewVO;
import org.zerock.wego.exception.AccessBlindException;
import org.zerock.wego.exception.ControllerException;
import org.zerock.wego.service.common.CommentService;
import org.zerock.wego.service.common.FavoriteService;
import org.zerock.wego.service.common.FileService;
import org.zerock.wego.service.info.SanInfoService;
import org.zerock.wego.service.review.ReviewService;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RequiredArgsConstructor

@RequestMapping("/review")
@Controller
public class ReviewController {

	private final SanInfoService sanInfoService;
	private final ReviewService reviewService;
	private final CommentService commentService;
	private final FileService fileService;
	private final FavoriteService favoriteService;

	@GetMapping("")
	public String openReview(Model model) throws ControllerException {
		log.trace("openReview({}) invoked.", model);

		try {
			List<ReviewViewVO> reviewList = this.reviewService.getList();

			model.addAttribute("reviewList", reviewList);

			return "review/review";
		} catch (Exception e) {
			throw new ControllerException(e);
		} // try-catch
	} // openReview

	@GetMapping(path = "/{reviewId}")
	public ModelAndView showDetailById(@PathVariable("reviewId") Integer reviewId,
			@SessionAttribute("__AUTH__") UserVO user, PageInfo target) throws Exception {
		log.trace("showDetail({}, {}) invoked.", reviewId, target);

		target.setTargetGb("SAN_REVIEW");
		target.setTargetCd(reviewId);

		ModelAndView mav = new ModelAndView();

		ReviewViewVO review = this.reviewService.getById(reviewId);
		Integer userId = user.getUserId();

		// TO_DO : 내글이면 블라인드 되도 보여야되는데 왜 막히냐 ?
		if ((review.getReportCnt() >= 5) && review.getUserId() != userId) {
			throw new AccessBlindException();
		} // if

		// TO_DO : 좋아요 바뀌면 바꿔야됨
		FavoriteDTO favorite = new FavoriteDTO();
		favorite.setTargetGb("SAN_REVIEW");
		favorite.setTargetCd(reviewId);
		favorite.setUserId(userId);

		boolean isFavorite = this.favoriteService.isFavoriteInfo(favorite);

		int commentCount = this.commentService.getTotalCountByTarget(target);

		LinkedBlockingDeque<CommentViewVO> comments = this.commentService.getCommentOffsetByTarget(target, 0);

		/* 후기글 사진 넣는거 필요함 */
		mav.addObject("review", review);
		mav.addObject("isFavorite", isFavorite);
		mav.addObject("commentCount", commentCount);
//			mav.addObject("userPic", userPic);

		if (comments != null) {

			mav.addObject("comments", comments);
		} // if

		ObjectMapper objectMapper = new ObjectMapper();
		String targetJson = objectMapper.writeValueAsString(target);
		mav.addObject("target", targetJson);

		mav.setViewName("/review/detail");

		return mav;
	}// viewReviewDetail

	@DeleteMapping(path = "/{reviewId}", produces = "text/plain; charset=UTF-8")
	public ResponseEntity<String> removeById(@PathVariable("reviewId") Integer reviewId) throws ControllerException {
		log.trace("removeById({}) invoked.", reviewId);

		try {
			this.reviewService.removeById(reviewId);
//			this.fileService.isRemoveByTarget("SAN_REVIEW", reviewId); 
			return ResponseEntity.ok("🗑 후기글이 삭제되었습니다.️"); // 인코딩해서 넣던가 안넣던가

		} catch (Exception e) {
			return ResponseEntity.badRequest().build();
		} // try-catch
	}// removeReview

	@GetMapping(path = "/modify/{reviewId}")
	public String modify(@SessionAttribute("__AUTH__") UserVO auth, @PathVariable("reviewId") Integer reviewId,
			Model model) throws Exception {
		log.trace("modify(auth, reviewId, model) invoked.");

		try {
			ReviewViewVO reviewVO = this.reviewService.getById(reviewId);
			Integer postUserId = reviewVO.getUserId();

			if (!auth.getUserId().equals(postUserId)) {
				throw new ControllerException("잘못된 접근입니다.");
			} // if

			List<FileVO> fileVO = this.fileService.getList("SAN_REVIEW", reviewId);

			model.addAttribute("review", reviewVO);
			model.addAttribute("fileList", fileVO);

			return "/review/modify";
		} catch (Exception e) {
			throw new ControllerException(e);
		} // try-catch
	} // modify

	@PostMapping("/modify")
	public String modify(@SessionAttribute("__AUTH__") UserVO auth, Integer sanReviewId, String sanName,
			@RequestParam(value = "imgFiles", required = false) List<MultipartFile> newImageFiles,
			@RequestParam(value = "oldImgFiles", required = false) String oldImageFiles,
			@RequestParam(value = "imgOrder", required = false) String imageOrder, ReviewDTO reviewDTO, FileDTO fileDTO)
			throws ControllerException {
		log.trace("modify(auth, sanReviewId, sanName, newImageFiles, oldImageFiles, reviewDTO, fileDTO) invoked.");

		try {
			Integer sanId = this.sanInfoService.getIdBySanName(sanName);

			reviewDTO.setSanInfoId(sanId);

			this.reviewService.modify(reviewDTO);
			
			List<String> oldFiles = Arrays.asList(oldImageFiles.split(","));
			List<String> order = Arrays.asList(imageOrder.split(","));
			
			this.fileService.isChangeImage(newImageFiles, oldFiles, order, "SAN_REVIEW", sanReviewId, fileDTO);

			return "redirect:/review/" + reviewDTO.getSanReviewId();
		} catch (Exception e) {
			throw new ControllerException(e);
		} // try-catch
	} // modify

	@GetMapping("/register")
	public String register(@SessionAttribute("__AUTH__") UserVO auth) {
		log.trace("register() invoked.");

		return "/review/register";
	} // register

	@PostMapping("/register")
	public String register(@SessionAttribute("__AUTH__") UserVO auth, String sanName,
			@RequestParam(value = "imgFiles", required = false) List<MultipartFile> imageFiles, ReviewDTO reviewDTO,
			FileDTO fileDTO) throws ControllerException {
		log.trace("register(auth, sanName, imageFiles, reviewDTO, fileDTO, posted, response) invoked.");

		try {
			Integer sanId = this.sanInfoService.getIdBySanName(sanName);

			reviewDTO.setSanInfoId(sanId);

			reviewDTO.setUserId(auth.getUserId());

			this.reviewService.register(reviewDTO);

			if (imageFiles != null) {
				boolean isImageUploadSuccess = this.fileService.isImageRegister(imageFiles, "SAN_REVIEW",
						reviewDTO.getSanReviewId(), fileDTO);
				log.info("isImageUploadSuccess: {}", isImageUploadSuccess);
			} // if

			return "redirect:/review/" + reviewDTO.getSanReviewId();
		} catch (Exception e) {
			throw new ControllerException(e);
		} // try-catch
	} // register
}// end class
