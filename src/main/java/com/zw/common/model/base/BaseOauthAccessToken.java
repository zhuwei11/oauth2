package com.zw.common.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings("serial")
public abstract class BaseOauthAccessToken<M extends BaseOauthAccessToken<M>> extends Model<M> implements IBean {

	public void setId(java.lang.Integer id) {
		set("id", id);
	}
	
	public java.lang.Integer getId() {
		return getInt("id");
	}

	public void setAppId(java.lang.Integer appId) {
		set("app_id", appId);
	}
	
	public java.lang.Integer getAppId() {
		return getInt("app_id");
	}

	public void setUserId(java.lang.Integer userId) {
		set("user_id", userId);
	}
	
	public java.lang.Integer getUserId() {
		return getInt("user_id");
	}

	public void setAccessToken(java.lang.String accessToken) {
		set("access_token", accessToken);
	}
	
	public java.lang.String getAccessToken() {
		return getStr("access_token");
	}

	public void setRefreshTokenId(java.lang.Integer refreshTokenId) {
		set("refresh_token_id", refreshTokenId);
	}
	
	public java.lang.Integer getRefreshTokenId() {
		return getInt("refresh_token_id");
	}

	public void setExpiresAt(java.util.Date expiresAt) {
		set("expires_at", expiresAt);
	}
	
	public java.util.Date getExpiresAt() {
		return get("expires_at");
	}

	public void setCreatedAt(java.util.Date createdAt) {
		set("created_at", createdAt);
	}
	
	public java.util.Date getCreatedAt() {
		return get("created_at");
	}

	public void setUpdatedAt(java.util.Date updatedAt) {
		set("updated_at", updatedAt);
	}
	
	public java.util.Date getUpdatedAt() {
		return get("updated_at");
	}

}