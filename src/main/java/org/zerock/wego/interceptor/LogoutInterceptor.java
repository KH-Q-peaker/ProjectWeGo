package org.zerock.wego.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.zerock.wego.config.SessionConfig;
import org.zerock.wego.domain.common.UserVO;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;


@Log4j2
@RequiredArgsConstructor

@Component("logoutInterceptor")
public class LogoutInterceptor 
	implements HandlerInterceptor{
	

	@Override
	public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler)
			throws Exception {
		log.trace("preHandle(req, res, handler) invoked \n \t\t\t\t>>>>>>>> requestURI : {}", req.getRequestURI());

		HttpSession session = req.getSession(false);

		session.removeAttribute(SessionConfig.AUTH_KEY_NAME);
		
		return true;
	} // preHandle



} // end class
