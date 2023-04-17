package org.zerock.wego.controller;

import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.zerock.wego.domain.CommentViewVO;
import org.zerock.wego.domain.FavoriteDTO;
import org.zerock.wego.domain.FileDTO;
import org.zerock.wego.domain.JoinDTO;
import org.zerock.wego.domain.PageInfo;
import org.zerock.wego.domain.PartyDTO;
import org.zerock.wego.domain.PartyViewVO;
import org.zerock.wego.domain.UserVO;
import org.zerock.wego.exception.ControllerException;
import org.zerock.wego.exception.NotFoundPageException;
import org.zerock.wego.service.CommentService;
import org.zerock.wego.service.FavoriteService;
import org.zerock.wego.service.FileService;
import org.zerock.wego.service.JoinService;
import org.zerock.wego.service.PartyService;
import org.zerock.wego.service.ReportService;
import org.zerock.wego.service.SanInfoService;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RequiredArgsConstructor

@RequestMapping("/party")
@Controller
public class PartyController {

	private final PartyService partyService;
	private final CommentService commentService;
	private final JoinService joinService;
	private final SanInfoService sanInfoService;
	private final FileService fileService;
	private final FavoriteService favoriteService;
	private final ReportService reportService;

	@GetMapping("")
	public String openParty(Model model) throws ControllerException {
		log.trace("openParty({}) invoked.", model);

		try {
			List<PartyViewVO> partyList = this.partyService.getList();

			model.addAttribute("partyList", partyList);

			return "party/party";
		} catch (Exception e) {
			throw new ControllerException(e);
		} // try-catch
	} // openParty

	@ModelAttribute("target")
	PageInfo createPageInfo(Integer partyId) {
		log.trace("createPageInfo() invoked.");

		PageInfo target = new PageInfo();

		target.setTargetGb("SAN_PARTY");
		target.setTargetCd(partyId);

		return target;
	}// createdBoardDTO

	// 모집글 상세 조회
	@GetMapping("/{partyId}")
	public ModelAndView showDetailById(@PathVariable("partyId") Integer partyId,
			@SessionAttribute("__AUTH__") UserVO user, PageInfo target) throws Exception {
		log.trace("showDetailById({}, {}) invoked.", partyId, user);

		target = this.createPageInfo(partyId);

		ModelAndView mav = new ModelAndView();

		PartyViewVO party = this.partyService.getById(partyId);

		if (party == null) {
			throw new NotFoundPageException("party not found : " + partyId);
		} // if

		Integer userId = user.getUserId();

		JoinDTO join = new JoinDTO();
		join.setSanPartyId(partyId);
		join.setUserId(userId);

		boolean isJoin = this.joinService.isJoin(join);

//			boolean isLike = this.likeService.isUserLiked(target, userId);
		FavoriteDTO favorite = new FavoriteDTO();
		favorite.setTargetGb("SAN_PARTY");
		favorite.setTargetCd(partyId);
		favorite.setUserId(userId);

		boolean isFavorite = this.favoriteService.isFavoriteInfo(favorite);

		int commentCount = this.commentService.getTotalCountByTarget(target);

		LinkedBlockingDeque<CommentViewVO> comments = commentService.getCommentOffsetByTarget(target, 0);

		mav.addObject("party", party);
		mav.addObject("isJoin", isJoin);
		mav.addObject("isFavorite", isFavorite);
		mav.addObject("commentCount", commentCount);
//			mav.addObject("userPic", userPic);
//			mav.addObject("partyImg", partyImg);

		if (comments != null) {

			mav.addObject("comments", comments);
		} // if

		ObjectMapper objectMapper = new ObjectMapper();
		String targetJson = objectMapper.writeValueAsString(target);
		mav.addObject("target", targetJson);

		mav.setViewName("/party/detail");

		return mav;

	}// showDetailById

	@GetMapping(path = "/modify/{partyId}")
	public String modify(@SessionAttribute("__AUTH__") UserVO auth, 
			@PathVariable("partyId") Integer partyId,
			Model model) throws ControllerException {
		log.trace("modify(auth, partyId, model) invoked.");

		try {
			Integer postUserId = this.partyService.getUserIdByPartyId(partyId);

			if (auth == null) {
				return "redirect:/login";
			} // if
			
			if (!auth.getUserId().equals(postUserId)) {
				throw new ControllerException("잘못된 접근입니다.");
			} // if

			PartyViewVO vo = this.partyService.getById(partyId);
			model.addAttribute("party", vo);

			return "/party/modify";
		} catch (Exception e) {
			throw new ControllerException(e);
		} // try-catch
	} // detailAndModify

	// 모집글 삭제
	@Transactional
	@DeleteMapping(path = "/{partyId}", produces = "text/plain; charset=UTF-8")
	public ResponseEntity<String> removeById(@PathVariable Integer partyId) throws Exception {
		log.trace("removeById({}) invoked.", partyId);

		boolean isPartyRemoved = this.partyService.isRemoveById(partyId);
		boolean isFileRemoved = this.fileService.isRemoveByTarget("SAN_PARTY", partyId);
		boolean isLikeRemoved = this.favoriteService.removeAllByTarget("SAN_PARTY", partyId);
		this.reportService.removeByTarget("SAN_PARTY", partyId);
//			boolean isJoinRemoved = this.joinService.isJoinCancled(partyId, userId);

		boolean isSuccess = isPartyRemoved && isFileRemoved && isLikeRemoved;

		if (isSuccess) {
			return ResponseEntity.ok("🗑 모집글이 삭제되었습니다.️");

		} else {
			return ResponseEntity.badRequest().build();
		} // if-else
	}// removeReview

	@PostMapping("/modify")
	public String modify(@SessionAttribute("__AUTH__") UserVO auth, Integer sanPartyId, String sanName,
			@RequestParam(value = "imgFile", required = false) List<MultipartFile> imageFiles, String date, String time,
			PartyDTO partyDTO, FileDTO fileDTO) throws ControllerException {
		log.trace("modify({}, {}, {}, {}, {}, {}, {}) invoked.", auth, sanPartyId, sanName, imageFiles, date, time,
				partyDTO);

		try {
			if (auth == null) {
				return "redirect:/login";
			} // if
			
			if (auth.getUserId() != partyDTO.getUserId()) {
				throw new ControllerException("잘못된 접근입니다.");
			} // if

			// TODO: 산이름으로 산ID 조회는 유틸 서비스로 분리 필요
			Integer sanId = this.sanInfoService.getIdBySanName(sanName);
			partyDTO.setSanInfoId(sanId);

			Timestamp dateTime = Timestamp.valueOf(date + " " + time + ":00");
			partyDTO.setPartyDt(dateTime);

			boolean isModifySuccess = this.partyService.modify(partyDTO);
			log.info("isModifySuccess: {}", isModifySuccess);

			if (imageFiles != null) {
				boolean isChangeImgeSuccess = this.fileService.isChangeImage(imageFiles, "SAN_PARTY",
						partyDTO.getSanPartyId(), fileDTO);
				log.info("isChangeImgeSuccess: {}", isChangeImgeSuccess);
			} // if

			return "redirect:/party/" + partyDTO.getSanPartyId();
		} catch (Exception e) {
			throw new ControllerException(e);
		} // try-catch
	} // modify

	@GetMapping("/register")
	public String register(@SessionAttribute("__AUTH__") UserVO auth) {
		log.trace("register() invoked.");

		if (auth == null) {
			return "redirect:/login";
		} // if

		return "/party/register";
	} // register

	@PostMapping("/register")
	public String register(@SessionAttribute("__AUTH__") UserVO auth, String sanName,
			@RequestParam(value = "imgFile", required = false) List<MultipartFile> imageFiles, String date, String time,
			PartyDTO partyDTO, FileDTO fileDTO, @CookieValue(value = "posted", required = false) boolean posted,
			HttpServletResponse response) throws ControllerException {
		log.trace("register({}, {}, {}, {}, {}, {}, {}) invoked.", auth, sanName, imageFiles, date, time, partyDTO,
				fileDTO);

		try {
			if (auth == null) {
				return "redirect:/login";
			} // if

			if (!posted) {
				Cookie cookie = new Cookie("posted", "true");
				cookie.setMaxAge(30);
				response.addCookie(cookie);
			} // if

			partyDTO.setUserId(auth.getUserId());

			Integer sanId = this.sanInfoService.getIdBySanName(sanName);
			partyDTO.setSanInfoId(sanId);

			Timestamp dateTime = Timestamp.valueOf(date + " " + time + ":00");
			partyDTO.setPartyDt(dateTime);

			boolean isSuccess = this.partyService.register(partyDTO);
			log.info("isSuccess: {}", isSuccess);

			if (imageFiles != null) {
				boolean isImageUploadSuccess = this.fileService.isImageRegister(imageFiles, "SAN_PARTY",
						partyDTO.getSanPartyId(), fileDTO);
				log.info("isImageUploadSuccess: {}", isImageUploadSuccess);
			} // if

			return "redirect:/party/" + partyDTO.getSanPartyId();
		} catch (Exception e) {
			throw new ControllerException(e);
		} // try-catch
	} // register

} // end class
