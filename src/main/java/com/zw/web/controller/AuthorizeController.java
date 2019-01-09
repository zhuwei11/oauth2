package com.zw.web.controller;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.oltu.oauth2.as.issuer.MD5Generator;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.request.OAuthAuthzRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;

import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.plugin.ehcache.CacheKit;
import com.zw.common.constant.SysConstant;
import com.zw.common.dto.UserSession;
import com.zw.common.model.OauthApp;
import com.zw.config.SysConfig;
import com.zw.service.AuthorizeService;
import com.zw.service.ResultCode;

public class AuthorizeController extends Controller {
	
	  private final static Logger LOG = Logger.getLogger(AuthorizeController.class);

	  @Inject
	  AuthorizeService authorizeService;
	  
	  public void index() {
		  
		  try {
		  
			OAuthAuthzRequest oauthRequest = new OAuthAuthzRequest(getRequest());
			OauthApp oauthApp = authorizeService.findByCode(Integer.parseInt(oauthRequest.getClientId()));
			if(oauthApp == null && !oauthRequest.getClientId().equals("0")) {
                OAuthResponse oauthResponse = OAuthASResponse
                                              .errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
                                              .setError(OAuthError.CodeResponse.ACCESS_DENIED)
                                              .setErrorDescription(OAuthError.CodeResponse.UNAUTHORIZED_CLIENT)
                                              .buildJSONMessage();
                LOG.error("oauthRequest.getRedirectURI() : "+oauthRequest.getRedirectURI()+" oauthResponse.getBody() : "+oauthResponse.getBody());
                renderError(404);
                return;
            }
			
		 	Boolean isLogin = getParaToBoolean("isLogin",false);
		 	
            //验证用户是否已登录
            if(getUserSession() == null && !isLogin) {
				setAttr("clientName",oauthApp.getAppName());
				setAttr("response_type",oauthRequest.getResponseType());
				setAttr("client_id",oauthRequest.getClientId());
				setAttr("redirect_uri",oauthRequest.getRedirectURI());
				setAttr("scope",oauthRequest.getScopes());
				render(SysConfig.BASE_VIEW_PATH+"/authorize/login.html");
				return;
            }
            
            if(getUserSession() == null && isLogin) {
            	ResultCode resultCode = authorizeService.doLogin(getRequest());
        		if(resultCode.getCode() != ResultCode.SUCCESS) {
        			renderJson(resultCode);
        			return;
        		}
            }
	            
           //生成授权码 UUIDValueGenerator OR MD5Generator
           String authorizationCode = new OAuthIssuerImpl(new MD5Generator()).authorizationCode();
           //把授权码存入缓存
           CacheKit.put(SysConstant.CACHE_NAME_OAUTH2_AUTHORIZATION_STORE, authorizationCode, getUserSession().getUserId());
           
           //构建oauth2授权返回信息
           OAuthResponse oauthResponse = OAuthASResponse.authorizationResponse(getRequest(),HttpServletResponse.SC_FOUND)
					                                         .setCode(authorizationCode)
					                                         .location(oauthRequest.getParam(OAuth.OAUTH_REDIRECT_URI))
					                                         .buildQueryMessage();          
    	   //申请令牌成功重定向到客户端页
    	   redirect(oauthResponse.getLocationUri(), true);
	  
	  } catch (OAuthSystemException e) {
		LOG.error(e.getMessage(), e);
		renderError(405);
	  } catch (OAuthProblemException e) {
		LOG.error(e.getMessage(), e);
		renderError(405);
	 }
  }
	  
	  
    
    public UserSession getUserSession() {
		return (UserSession)getSessionAttr(SysConstant.SESSION_ID_KEY);
	}
	  
}
