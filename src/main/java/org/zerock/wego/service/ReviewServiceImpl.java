package org.zerock.wego.service;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.zerock.wego.domain.ReviewDTO;
import org.zerock.wego.domain.ReviewViewVO;
import org.zerock.wego.exception.ServiceException;
import org.zerock.wego.mapper.ReviewMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;


@Log4j2
@RequiredArgsConstructor
@Service
public class ReviewServiceImpl implements ReviewService, InitializingBean{
	
	
	private final ReviewMapper reviewMapper;
	
	
	@Override
	public void afterPropertiesSet() throws ServiceException { // 1회성 전처리
		log.trace("afterPropertiesSet() invoked.");
		
		try {
			Objects.requireNonNull(this.reviewMapper);
			log.info("this.mapper: {}", this.reviewMapper);
		} catch (Exception e) {
			throw new ServiceException(e);
		} // try-catch
	} // afterPropertiesSet
	
	
	@Override
	public List<ReviewViewVO> getList() throws ServiceException {
		log.trace("getList() invoked.");
		
		try {
			return this.reviewMapper.selectAll();
		} catch (Exception e) {
			throw new ServiceException(e);
		} // try-catch
	} // getList
	
	
	@Override
	public Set<ReviewViewVO> getRandom10List() throws ServiceException {
		log.trace("getRandom10List() invoked.");
		
		try {
			return this.reviewMapper.selectRandom10();
		} catch (Exception e) {
			throw new ServiceException(e);
		} // try-catch
	} // getList
	
	
	// 특정 후기글 조회 
	@Override
	public ReviewViewVO getById(Integer reviewId) throws ServiceException {
		log.trace("getById({}) invoked.", reviewId);	
		
		try {
			ReviewViewVO review = this.reviewMapper.selectById(reviewId);
			Objects.requireNonNull(review);

			
			return review;
		} catch (Exception e) {
			throw new ServiceException(e);
		} // try-catch
	} // get
	
	
	// 특정 후기글 삭제
	@Override
	public boolean isRemoved(Integer reviewId) throws ServiceException {
		log.trace("isRemoved({}) invoked.", reviewId);
		
		try {
			
			return this.reviewMapper.delete(reviewId) == 1;
			
		} catch (Exception e) {
			throw new ServiceException(e);
		} // try-catch
	} // remove
	
	
	@Override
	public boolean isRegistered(ReviewDTO dto) throws ServiceException {
		log.trace("register({}) invoked.");
		
		try {
			return this.reviewMapper.insert(dto) == 1;
		} catch (Exception e) {
			throw new ServiceException(e);
		} // try-catch
	} // register
	
	
	@Override
	public boolean isModified(ReviewDTO dto) throws ServiceException {
		log.trace("modify({}) invoked.");
		
		try {
			return this.reviewMapper.update(dto) == 1;
		} catch (Exception e) {
			throw new ServiceException(e);
		} // try-catch
	} // modify

}// end class