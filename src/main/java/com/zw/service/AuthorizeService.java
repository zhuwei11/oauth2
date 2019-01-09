package com.zw.service;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import com.jfinal.kit.StrKit;
import com.zw.common.constant.SysConstant;
import com.zw.common.dto.UserSession;
import com.zw.common.model.OauthAccessToken;
import com.zw.common.model.OauthApp;
import com.zw.common.model.OauthRefreshToken;
import com.zw.common.model.UserInfo;
import com.zw.common.tools.DateUtil;

public class AuthorizeService {
	
	private OauthApp oauthAppDao = new OauthApp().dao();
	private UserInfo userInfoDao = new UserInfo().dao();
	private OauthAccessToken oauthAccessTokenDao = new OauthAccessToken().dao();
	private OauthRefreshToken oauthRefreshTokenDao = new OauthRefreshToken().dao();
	
	public OauthApp findByCode(Integer appCode) {
		return oauthAppDao.findFirst("select * from oauth_app where app_code = ? limit 0,1", appCode);
	}
	
	public UserInfo findByLogin(String loginName) {
		return userInfoDao.findFirst("select * from user_info where loginname = ? limit 0,1", loginName);
	}
	
	public UserInfo findById(Integer id) {
		return userInfoDao.findById(id);
	}
	
	public OauthAccessToken findByRefId(Integer id) {
		return oauthAccessTokenDao.findFirst("select * from oauth_access_token where refresh_token_id = ?", id);
	}
	
	public OauthAccessToken findByToken(String token) {
		return oauthAccessTokenDao.findFirst("select * from oauth_access_token where access_token = ?", token);
	}
	
	public OauthRefreshToken findByReToken(String token) {
		return oauthRefreshTokenDao.findFirst("select * from oauth_refresh_token where refresh_token = ?", token);
	}
	
	public ResultCode doLogin(HttpServletRequest request) {
		try {
			
			String username = request.getParameter("username");
            String password = request.getParameter("password");
            
            if(StrKit.isBlank(username) || StrKit.isBlank(password)) {
            	ResultCode resultCode = new ResultCode(ResultCode.FAILURE, "帐号或者密码不能为空！");
            	return resultCode;
            }
            
            UserInfo userInfo = findByLogin(username);
            if(userInfo == null || !password.equals(userInfo.getPassword())) {
            	ResultCode resultCode = new ResultCode(ResultCode.FAILURE, "账号或密码错误！");
            	return resultCode;
            }

            UserSession session = new UserSession();
			session.setUsername(userInfo.getUsername());
			session.setUserId(userInfo.getId());
			session.setRealname(userInfo.getUsername());
//			session.setLastLoginTime((Date)userInfo.get("last_login_time"));
//			session.setLastLoginIp(userInfo.getStr("last_login_ip"));
			session.setNickname(userInfo.getUsername());
			request.getSession().setAttribute(SysConstant.SESSION_ID_KEY, session);				
            
			ResultCode resultCode = new ResultCode(ResultCode.SUCCESS, "登录成功");
			return resultCode;
        } catch(Exception e) {
        	ResultCode resultCode = new ResultCode(ResultCode.FAILURE, "登录异常，请联系管理员");
			return resultCode;
        }
	}
	
	public void saveToken(long userId, OauthApp oauthApp, final String accessToken, String refreshToken) {
		OauthAccessToken oauthAccessToken = oauthAccessTokenDao.findFirst("select * from oauth_access_token where user_id=?", userId);
		OauthRefreshToken oauthRefreshToken = null;
		
		if(oauthAccessToken != null) {
			oauthRefreshToken = oauthRefreshTokenDao.findById(oauthAccessToken.getInt("refresh_token_id"));
			if(oauthRefreshToken != null) {
				oauthRefreshToken.setRefreshToken(refreshToken);
		        oauthRefreshToken.setExpiresAt(DateUtil.daysAddOrSub(new Date(), SysConstant.OAUTH2_REFRESH_TOKEN_EXPIRES_TIME)); // 有效期30天
		        oauthRefreshToken.update();
		        
			} else {
				oauthRefreshToken = new OauthRefreshToken();
		        oauthRefreshToken.setRefreshToken(refreshToken);
		        oauthRefreshToken.setExpiresAt(DateUtil.daysAddOrSub(new Date(), SysConstant.OAUTH2_REFRESH_TOKEN_EXPIRES_TIME)); // 有效期30天
		        oauthRefreshToken.setCreatedAt(new Date());
		        oauthRefreshToken.setAppId(oauthApp.getInt("id"));
		        oauthRefreshToken.save();
			}
		    oauthAccessToken.setAccessToken(accessToken);
		    oauthAccessToken.setRefreshTokenId(oauthRefreshToken.getInt("id"));
		    oauthAccessToken.setExpiresAt(new Date(System.currentTimeMillis()+SysConstant.OAUTH2_ACCESS_TOKEN_EXPIRES_TIME*1000));
		    oauthAccessToken.setUpdatedAt(new Date());
		    oauthAccessToken.update();
		} else {
			oauthRefreshToken = new OauthRefreshToken();
		    oauthRefreshToken.setRefreshToken(refreshToken);
		    oauthRefreshToken.setExpiresAt(DateUtil.daysAddOrSub(new Date(), SysConstant.OAUTH2_REFRESH_TOKEN_EXPIRES_TIME)); // 有效期30天
		    oauthRefreshToken.setCreatedAt(new Date());
		    oauthRefreshToken.setAppId(oauthApp.getId());
		    oauthRefreshToken.save();
		    
			oauthAccessToken = new OauthAccessToken();
		    oauthAccessToken.setAppId(oauthApp.getId());
		    oauthAccessToken.setAccessToken(accessToken);
		    oauthAccessToken.setUserId(new Integer((int) userId));
		    oauthAccessToken.setRefreshTokenId(oauthRefreshToken.getId());
		    oauthAccessToken.setExpiresAt(new Date(System.currentTimeMillis()+SysConstant.OAUTH2_ACCESS_TOKEN_EXPIRES_TIME*1000));
		    oauthAccessToken.setCreatedAt(new Date());
		    oauthAccessToken.setUpdatedAt(new Date());
		    oauthAccessToken.save();
		}
	}
}
