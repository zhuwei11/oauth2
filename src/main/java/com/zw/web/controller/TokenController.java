package com.zw.web.controller;
import java.io.IOException;
import java.util.Date;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.oltu.oauth2.as.issuer.MD5Generator;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuer;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.request.OAuthTokenRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.GrantType;

import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.plugin.ehcache.CacheKit;
import com.zw.common.constant.SysConstant;
import com.zw.common.model.OauthAccessToken;
import com.zw.common.model.OauthApp;
import com.zw.common.model.OauthRefreshToken;
import com.zw.common.tools.DateUtil;
import com.zw.service.AuthorizeService;

/**
 * 令牌获取与更新
 * @author defier
 * 2014年12月24日 上午12:24:35
 * @version 1.0
 */
public class TokenController extends Controller {
	
    private static Logger LOG = Logger.getLogger(TokenController.class);
    
    @Inject
	AuthorizeService authorizeService;

    /**
     * 认证服务器申请令牌(AccessToken) [验证client_id、client_secret、authcode的正确性或更新令牌 refresh_token]
     * @param request
     * @param response
     * @return
     * @url http://localhost:8080/oauth2/access_token?client_id={AppKey}&client_secret={AppSecret}&grant_type=authorization_code&redirect_uri={YourSiteUrl}&code={code}
     */
    public void index(){
        OAuthIssuer oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());
        try{
            //构建oauth2请求
            OAuthTokenRequest oauthRequest = new OAuthTokenRequest(getRequest());
            //验证redirecturl格式是否合法 (8080端口测试)
            if (!oauthRequest.getRedirectURI().contains(":7070")&&!Pattern.compile("^[a-zA-Z]+://(\\w+(-\\w+)*)(\\.(\\w+(-\\w+)*))*(\\?\\s*)?$").matcher(oauthRequest.getRedirectURI()).matches()) {
                OAuthResponse oauthResponse = OAuthASResponse
                                              .errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
                                              .setError(OAuthError.CodeResponse.INVALID_REQUEST)
                                              .setErrorDescription(OAuthError.OAUTH_ERROR_URI)
                                              .buildJSONMessage();
                renderJson(oauthResponse.getBody());
                return;
            }
            OauthApp oauthApp = authorizeService.findByCode(Integer.parseInt(oauthRequest.getClientId()));
            //验证appkey是否正确
            if (oauthApp == null){
                OAuthResponse oauthResponse = OAuthASResponse
                                              .errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
                                              .setError(OAuthError.CodeResponse.ACCESS_DENIED)
                                              .setErrorDescription(OAuthError.CodeResponse.UNAUTHORIZED_CLIENT)
                                              .buildJSONMessage();
                renderJson(oauthResponse.getBody());
                return;
            }
            //验证客户端安全AppSecret是否正确
            if (!oauthApp.getAppSecret().equals(oauthRequest.getClientSecret())) {
                OAuthResponse oauthResponse = OAuthASResponse
                                              .errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
                                              .setError(OAuthError.TokenResponse.UNAUTHORIZED_CLIENT)
                                              .setErrorDescription(SysConstant.INVALID_CLIENT_SECRET)
                                              .buildJSONMessage();
                renderJson(oauthResponse.getBody());
                return;
            }
            
            String authzCode = oauthRequest.getCode();
            
            //验证AUTHORIZATION_CODE , 其他的还有PASSWORD 或 REFRESH_TOKEN (考虑到更新令牌的问题，在做修改)
            if (GrantType.AUTHORIZATION_CODE.name().equalsIgnoreCase(oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE))) {
                if (CacheKit.get(SysConstant.CACHE_NAME_OAUTH2_AUTHORIZATION_STORE, authzCode) == null) {
                    OAuthResponse oauthResponse = OAuthASResponse
                                                  .errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
                                                  .setError(OAuthError.TokenResponse.INVALID_GRANT)
                                                  .setErrorDescription(SysConstant.INVALID_CLIENT_GRANT)
                                                  .buildJSONMessage();
                    renderJson(oauthResponse.getBody());
                    return;
                }
            }
            
            //生成token
            final String accessToken = oauthIssuerImpl.accessToken();
            String refreshToken = oauthIssuerImpl.refreshToken();
            long userId = CacheKit.get(SysConstant.CACHE_NAME_OAUTH2_AUTHORIZATION_STORE, authzCode);
           
            authorizeService.saveToken(userId, oauthApp, accessToken, refreshToken);
            
            LOG.info("accessToken : "+accessToken +"  refreshToken: "+refreshToken);
            //清除授权码 确保一个code只能使用一次
            CacheKit.remove(SysConstant.CACHE_NAME_OAUTH2_AUTHORIZATION_STORE, authzCode);
            
            //构建oauth2授权返回信息
            OAuthResponse oauthResponse = OAuthASResponse
                                          .tokenResponse(HttpServletResponse.SC_OK)
                                          .setAccessToken(accessToken)
                                          .setExpiresIn(SysConstant.OAUTH2_ACCESS_TOKEN_EXPIRES_TIME+"")
                                          .setRefreshToken(refreshToken)
                                          .buildJSONMessage();
            getResponse().setStatus(oauthResponse.getResponseStatus());
            renderJson(oauthResponse.getBody());
            
        } catch(Exception e) {
        	LOG.error("access token exception!!!!", e);
			try {
				OAuthResponse oauthResponse = OAuthResponse.errorResponse(HttpServletResponse.SC_UNAUTHORIZED).buildJSONMessage();
				getResponse().setStatus(oauthResponse.getResponseStatus());
	            renderJson(oauthResponse.getBody());
			} catch (OAuthSystemException e1) {
				LOG.error("OAuthSystemException", e);
			}
        }
    }


    

    /**
     * 刷新令牌
     * @param request
     * @param response
     * @throws IOException
     * @throws OAuthSystemException
     * @url http://localhost:8080/oauth2/refresh_token?client_id={AppKey}&grant_type=refresh_token&refresh_token={refresh_token}
     */
    public void refresh_token() {
        OAuthIssuer oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());
        try {
            //构建oauth2请求
            OAuthTokenRequest oauthRequest = new OAuthTokenRequest(getRequest());
            
            OauthApp oauthApp = authorizeService.findByCode(Integer.parseInt(oauthRequest.getClientId()));
           
            //验证appkey是否正确
            if (oauthApp == null){
                OAuthResponse oauthResponse = OAuthASResponse.errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
									                        .setError(OAuthError.CodeResponse.ACCESS_DENIED)
									                        .setErrorDescription(OAuthError.CodeResponse.UNAUTHORIZED_CLIENT)
									                        .buildJSONMessage();
                renderJson(oauthResponse.getBody());
                return;
            }
            //验证是否是refresh_token
            if (GrantType.REFRESH_TOKEN.name().equalsIgnoreCase(oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE))) {
                OAuthResponse oauthResponse = OAuthASResponse.errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
									                        .setError(OAuthError.TokenResponse.INVALID_GRANT)
									                        .setErrorDescription(SysConstant.INVALID_CLIENT_GRANT)
									                        .buildJSONMessage();
                renderJson(oauthResponse.getBody());
                return;
            }
            /*
            * 刷新access_token有效期
             access_token是调用授权关系接口的调用凭证，由于access_token有效期（目前为2个小时）较短，当access_token超时后，可以使用refresh_token进行刷新，access_token刷新结果有两种：
             1. 若access_token已超时，那么进行refresh_token会获取一个新的access_token，新的超时时间；
             2. 若access_token未超时，那么进行refresh_token不会改变access_token，但超时时间会刷新，相当于续期access_token。
             refresh_token拥有较长的有效期（30天），当refresh_token失效的后，需要用户重新授权。
            * */
            OauthRefreshToken oauthRefreshToken = authorizeService.findByReToken(oauthRequest.getRefreshToken());
            if (oauthRefreshToken == null || oauthRefreshToken.getTimestamp("expires_at").before(new Date())) { //refresh_token已超时
            	oauthRefreshToken.delete();
            	 OAuthResponse oauthResponse = OAuthASResponse.errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
									                         .setError(OAuthError.CodeResponse.ACCESS_DENIED)
									                         .setErrorDescription(OAuthError.CodeResponse.UNAUTHORIZED_CLIENT)
									                         .buildJSONMessage();
                 renderJson(oauthResponse.getBody());
                 return;
            }
            
            final String accessToken = oauthIssuerImpl.accessToken();
            String refreshToken = oauthIssuerImpl.refreshToken();
            LOG.info("access_Token : "+accessToken +"  refresh_Token: "+refreshToken);
            
        	OauthAccessToken oauthAccessToken = authorizeService.findByRefId(oauthRefreshToken.getInt("id"));
        	if(oauthAccessToken != null) {
        		if(oauthAccessToken.getTimestamp("expires_at").before(new Date())) {
        			oauthAccessToken.set("access_token", accessToken);
                    oauthAccessToken.set("expires_at", new Date(System.currentTimeMillis()+SysConstant.OAUTH2_ACCESS_TOKEN_EXPIRES_TIME*1000));
                    oauthAccessToken.set("updated_at", new Date());
                    oauthAccessToken.update();
                    
                    oauthRefreshToken.set("refresh_token", refreshToken);
                    oauthRefreshToken.set("expires_at", DateUtil.daysAddOrSub(new Date(), SysConstant.OAUTH2_REFRESH_TOKEN_EXPIRES_TIME)); // 有效期延长30天
                    oauthRefreshToken.update();
                    
                    //构建oauth2授权返回信息
                    OAuthResponse oauthResponse = OAuthASResponse.tokenResponse(HttpServletResponse.SC_OK)
        								                        .setAccessToken(accessToken)
        								                        .setExpiresIn(SysConstant.OAUTH2_ACCESS_TOKEN_EXPIRES_TIME+"")
        								                        .setRefreshToken(refreshToken)
        								                        .buildJSONMessage();
                    getResponse().setStatus(oauthResponse.getResponseStatus());
                    renderJson(oauthResponse.getBody());
                    
        		} else {
        			oauthAccessToken.delete();
        			OAuthResponse oauthResponse = OAuthResponse.errorResponse(HttpServletResponse.SC_UNAUTHORIZED).buildJSONMessage();
    				getResponse().setStatus(oauthResponse.getResponseStatus());
    	            renderJson(oauthResponse.getBody());
        		}
        	}
        } catch(Exception e) {
        	LOG.error("access token exception!!!!", e);
			try {
				OAuthResponse oauthResponse = OAuthResponse.errorResponse(HttpServletResponse.SC_UNAUTHORIZED).buildJSONMessage();
				getResponse().setStatus(oauthResponse.getResponseStatus());
	            renderJson(oauthResponse.getBody());
			} catch (OAuthSystemException e1) {
				LOG.error("OAuthSystemException", e);
			}
        }
    }



}