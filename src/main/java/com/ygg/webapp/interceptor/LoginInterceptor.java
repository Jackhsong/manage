package com.ygg.webapp.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.ygg.webapp.entity.ManagerEntity;


public class LoginInterceptor extends HandlerInterceptorAdapter{

	/**
	 * Handler执行完成之后调用这个方法
	 */
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception exc)
			throws Exception {

	}

	/**
	 * Handler执行之后，ModelAndView返回之前调用这个方法
	 */
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		super.postHandle(request, response, handler, modelAndView);

	}

	/**
	 * Handler执行之前调用这个方法
	 */
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		// 获取请求的URL
		String requestUrl = request.getRequestURI();
		//登陆页的自动放行
		String urll1="/admin/login";
		String urll2="/admin/getCode";
		String urll3="/admin/checkManager";
		ManagerEntity me=(ManagerEntity) request.getSession().getAttribute("Manager");
		if(me!=null){
			return true;
		}
		if (requestUrl.endsWith(urll1)||requestUrl.endsWith(urll2)||requestUrl.endsWith(urll3)) {
			return true;
		}
		if(me==null&&!requestUrl.endsWith(urll1)){
			response.sendRedirect(request.getContextPath()+"/admin/login");
		}

		return false;
	}
}
