package org.zerock.wego.controller;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
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
import org.zerock.wego.domain.common.PageInfo;
import org.zerock.wego.domain.common.UserVO;
import org.zerock.wego.domain.party.JoinDTO;
import org.zerock.wego.domain.party.PartyDTO;
import org.zerock.wego.domain.party.PartyViewVO;
import org.zerock.wego.exception.AccessBlindException;
import org.zerock.wego.exception.ControllerException;
import org.zerock.wego.exception.NotFoundPageException;
import org.zerock.wego.exception.OperationFailException;
import org.zerock.wego.service.common.CommentService;
import org.zerock.wego.service.common.FavoriteService;
import org.zerock.wego.service.common.FileService;
import org.zerock.wego.service.common.ReportService;
import org.zerock.wego.service.info.SanInfoService;
import org.zerock.wego.service.party.JoinService;
import org.zerock.wego.service.party.PartyService;

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

	
	// 모집글 상세 조회 
	@GetMapping("/{partyId}") 
	public ModelAndView showDetailById(@PathVariable("partyId")Integer partyId, 
										@SessionAttribute("__AUTH__")UserVO user,
										PageInfo pageInfo) throws Exception{
	log.trace("showDetailById() invoked.");
		
			pageInfo.setTargetGb("SAN_PARTY");
			pageInfo.setTargetCd(partyId);
			
			ModelAndView mav = new ModelAndView();
			
			PartyViewVO party = this.partyService.getById(partyId);
			Integer userId = user.getUserId();
			log.error(userId);
			log.error(party.getUserId());
			log.error(party.getUserId() != userId);
			if((!party.getUserId().equals(userId)) && (party.getReportCnt() >= 5)) {
				throw new AccessBlindException();
			} // if	
			
			JoinDTO join = new JoinDTO();
			join.setSanPartyId(partyId);
			join.setUserId(userId);
			
			boolean isJoin = this.joinService.isJoin(join);
			
			// TO_DO : 좋아요구현되면 바꾸기 
			FavoriteDTO favorite = new FavoriteDTO();
			favorite.setTargetGb("SAN_PARTY");
			favorite.setTargetCd(partyId);
			favorite.setUserId(userId);
			
			boolean isFavorite = this.favoriteService.isFavoriteInfo(favorite);
			
			int commentCount = this.commentService.getTotalCountByTarget(pageInfo);
			
			LinkedBlockingDeque<CommentViewVO> comments 
						= commentService.getCommentOffsetByTarget(pageInfo, 0);

			
			mav.addObject("party", party);
			mav.addObject("isJoin", isJoin);
			mav.addObject("isFavorite", isFavorite);
			mav.addObject("commentCount", commentCount);
			
			if(comments != null) {
				mav.addObject("comments", comments);
			}// if
			
			ObjectMapper objectMapper = new ObjectMapper();
			String pageInfoJson = objectMapper.writeValueAsString(pageInfo);
			mav.addObject("target", pageInfoJson);

			mav.setViewName("/party/detail");

			return mav;

	}// showDetailById
	
	
	@GetMapping(path = "/modify/{partyId}")
	public String modify(
			@SessionAttribute("__AUTH__")UserVO auth,
			@PathVariable("partyId") Integer partyId, Model model) 
			throws ControllerException { 
		log.trace("modify({}, {}, {}) invoked.", auth, partyId, model);

		try {
			Integer postUserId = this.partyService.getUserIdByPartyId(partyId);

			if (!auth.getUserId().equals(postUserId)) {
				throw new ControllerException("잘못된 접근입니다.");
			} // if
			
			PartyViewVO vo = this.partyService.getById(partyId);
			model.addAttribute("party", vo);
			
			return "/party/modify";
		} catch (Exception e) {
			throw new ControllerException(e);
		} // try-catch
	} // modify

	

	// 모집글 삭제
	@Transactional
	@DeleteMapping(path= "/{partyId}", produces= "text/plain; charset=UTF-8")
	public ResponseEntity<String> removeById(@PathVariable Integer partyId) throws Exception {
	log.trace("removeById({}) invoked.", partyId);

		try {
			this.partyService.removeById(partyId);
			this.fileService.isRemoveByTarget("SAN_PARTY", partyId);
			this.favoriteService.removeAllByTarget("SAN_PARTY", partyId);
			this.reportService.removeAllByTarget("SAN_PARTY", partyId);

			return ResponseEntity.ok("🗑 모집글이 삭제되었습니다.️");
			
		} catch (NotFoundPageException | OperationFailException e) {
			return ResponseEntity.badRequest().build();
		}// try-catch
	}// removeById
	

	@PostMapping("/modify")
	public String modify(
			@SessionAttribute("__AUTH__")UserVO auth,
			Integer sanPartyId, String sanName, 
			@RequestParam(value = "imgFile", required = false)List<MultipartFile> imageFiles, 
			String date, String time, PartyDTO partyDTO, FileDTO fileDTO
			) throws ControllerException { 
		log.trace("PostMapping - modify() invoked.");

		try {			
			// TODO: 산이름으로 산ID 조회는 유틸 서비스로 분리 필요
			Integer sanId = this.sanInfoService.getIdBySanName(sanName);
			partyDTO.setSanInfoId(sanId);

			Timestamp dateTime = Timestamp.valueOf(date + " " + time + ":00");
			partyDTO.setPartyDt(dateTime);
			
			boolean isModifySuccess = this.partyService.modify(partyDTO);
			log.info("isModifySuccess: {}", isModifySuccess);

			if (imageFiles != null) {
				List<String> oldImageFiles = Arrays.asList(this.fileService.getList("SAN_PARTY", partyDTO.getSanPartyId()).get(0).getFileName());
				List<String> order = Arrays.asList(imageFiles.get(0).getOriginalFilename());
				boolean isChangeImgeSuccess = this.fileService.isChangeImage(imageFiles, oldImageFiles, order, "SAN_PARTY",
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

		return "/party/register";
	} // register

	@PostMapping("/register")
	public String register(
			@SessionAttribute("__AUTH__")UserVO auth, String sanName, 
			@RequestParam(value = "imgFile", required = false)List<MultipartFile> imageFiles,
			String date, String time, PartyDTO partyDTO, FileDTO fileDTO
			) throws ControllerException {
		log.trace("PostMapping - register() invoked.");

		try {			
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
	
	// 참여 신청/취소 토글
	@PostMapping(path = "/{partyId}/join", produces = "text/plain; charset=UTF-8")
	ResponseEntity<String> joinOrCancleByPartyId(@PathVariable Integer partyId,
											@SessionAttribute("__AUTH__") UserVO user) throws Exception {
		log.trace("joinOrCancleByPartyId() invoked.");

		try {
			Integer userId = user.getUserId();

			JoinDTO join = new JoinDTO();
			join.setSanPartyId(partyId);
			join.setUserId(userId);

			this.joinService.createOrCancle(join);
			Integer currentCount = this.joinService.getCurrentCount(join);

			return ResponseEntity.ok(currentCount.toString());

		} catch (OperationFailException e) {
			return ResponseEntity.badRequest().body("모집 인원이 가득 찼습니다.");

		} catch (NotFoundPageException e) {
			return ResponseEntity.badRequest().body("해당 모집글을 찾을 수 없습니다.");
		} // try-catch
	}// joinOrCancleByPartyId
} // end class
