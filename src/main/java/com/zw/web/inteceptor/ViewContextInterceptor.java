package com.zw.web.inteceptor;

import javax.servlet.http.HttpServletRequest;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.zw.common.constant.SysConstant;
import com.zw.common.dto.UserSession;

public class ViewContextInterceptor implements Interceptor {


	@Override
	public void intercept(Invocation ai) {
		
		HttpServletRequest request = ai.getController().getRequest();
		UserSession session = (UserSession)request.getSession().getAttribute(SysConstant.SESSION_ID_KEY);
		if(session != null) {
			request.setAttribute("session", session);
		}
		ai.invoke();
	}

}
