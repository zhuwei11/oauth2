package com.zw.web.controller;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.ParameterStyle;
import org.apache.oltu.oauth2.rs.request.OAuthAccessResourceRequest;
import org.apache.oltu.oauth2.rs.response.OAuthRSResponse;

import com.alibaba.druid.util.StringUtils;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.zw.common.model.OauthAccessToken;
import com.zw.common.model.UserInfo;
import com.zw.service.AuthorizeService;

/**
 * OAuth2 资源服务
 * @author zw
 * 2014年12月24日 下午3:38:32
 * @version 1.0
 */
public class ResourceController extends Controller {
	
    private final static Logger LOG = Logger.getLogger(ResourceController.class);
    
    @Inject
	AuthorizeService authorizeService;
    
    /**
     * 获取用户基本信息
     */
    public void userInfo() {
        try {
            OAuthAccessResourceRequest oauthRequest = new OAuthAccessResourceRequest(getRequest(), ParameterStyle.QUERY);
            String accessToken = oauthRequest.getAccessToken();
            OauthAccessToken oauthAccessToken = authorizeService.findByToken(accessToken);
            if (StringUtils.isEmpty(accessToken) || oauthAccessToken == null || oauthAccessToken.getTimestamp("expires_at").before(new Date())) {
                OAuthResponse oauthResponse = OAuthRSResponse.errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
				                                              .setRealm("RESOURCE_SERVER_NAME")
				                                              .setError(OAuthError.ResourceResponse.INVALID_TOKEN)
				                                              .setErrorDescription(OAuthError.ResourceResponse.EXPIRED_TOKEN)
				                                              .buildHeaderMessage();
                getResponse().addDateHeader(OAuth.HeaderType.WWW_AUTHENTICATE, Long.parseLong(oauthResponse.getHeader(OAuth.HeaderType.WWW_AUTHENTICATE)));
            }
            //获得用户名
            
            UserInfo user = authorizeService.findById(oauthAccessToken.getUserId());
            
            renderJson("{user_id:"+user.getInt("id")+", login_name:'"+user.getStr("login_name")+"', nick_name:'"+user.getStr("nick_name")+"', real_name:'"+user.getStr("real_name")+"', "
            				+ "gender:"+user.getInt("gender")+", qq:'"+user.getStr("qq")+"', mobile:'"+user.getStr("mobile")+"',mobile_flag:"+user.getBoolean("mobile_flag")
            				+ ", mail:'"+user.getStr("mail")+"', avatar:'"+user.getStr("avatar")+"'"
            				+ ", province_name:'"+user.getStr("province_name")+"', city_name:'"+user.getStr("city_name")+"', county_name:'"+user.getStr("county_name")+"'}");
            
        } catch (Exception e) {
            LOG.error("ResourceController OAuthProblemException", e);
			try {
				OAuthResponse oauthResponse = OAuthRSResponse
				                              .errorResponse(HttpServletResponse.SC_UNAUTHORIZED)
				                              .setRealm("get_resource exception")
				                              .buildHeaderMessage();
				getResponse().addDateHeader(OAuth.HeaderType.WWW_AUTHENTICATE, Long.parseLong(oauthResponse.getHeader(OAuth.HeaderType.WWW_AUTHENTICATE)));
			
			} catch (OAuthSystemException e1) {
				e1.printStackTrace();
			}
        }
    }
}