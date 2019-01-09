package com.zw.common.dto;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * 用户session信息
 * @author defier.lai
 *
 * 2013-1-7 下午11:17:32
 */
public class UserSession {
	
	private boolean superFlag;
	/**
	 * 用户ID
	 */
	private long userId;
	
	/**
	 * 用户名
	 */
	private String username;
	/**
	 * 性别，1-男，2-女，0-未填写
	 */
	private int gender;
	/**
	 * 头像
	 */
	private String photo;
	/**
	 * 帐户名
	 */
	private String realname;
	/**
	 * 昵称
	 */
	private String nickname;
	/**
	 * 最后登录IP
	 */
	private String lastLoginIp;
	/**
	 * 最后登录时间
	 */
	private Date lastLoginTime;
	/**
	 * 登录IP
	 */
	private String loginIp;
	
	/**
	 * 操作列表
	 */
	private Set<String> operCodeSet = new HashSet<String>();
	
	 
	/**
	 * 菜单列表
	 */
	private Set<String> menuCodeSet = new HashSet<String>();

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}
	
	

	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}

	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}

	public String getRealname() {
		return realname;
	}

	public void setRealname(String realname) {
		this.realname = realname;
	}

	public String getLastLoginIp() {
		return lastLoginIp;
	}

	public void setLastLoginIp(String lastLoginIp) {
		this.lastLoginIp = lastLoginIp;
	}

	public Date getLastLoginTime() {
		return lastLoginTime;
	}

	public void setLastLoginTime(Date lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}

	public boolean isSuperFlag() {
		return superFlag;
	}

	public void setSuperFlag(boolean superFlag) {
		this.superFlag = superFlag;
	}
	
	public String getLoginIp() {
		return loginIp;
	}

	public void setLoginIp(String loginIp) {
		this.loginIp = loginIp;
	}

	public boolean hasOper(String operCode) {
		if(isSuperFlag()){
			return true;
		}
		return operCodeSet.contains(operCode);
	}
	
	public boolean hasAnyOper(String[] operCodes) {
		boolean f = false;
		for(String code : operCodes) {
			if(hasOper(code)) {
				f = true;
				break;
			}
		}
		return f;
	}
	
	public boolean hasMenu(String menuCode) {
		if(isSuperFlag()){
			return true;
		}
		return menuCodeSet.contains(menuCode);
	}

	public void setOperCodeSet(Set<String> operCodeSet) {
		this.operCodeSet = operCodeSet;
	}

	public void setMenuCodeSet(Set<String> menuCodeSet) {
		this.menuCodeSet = menuCodeSet;
	}

	public Set<String> getMenuCodeSet() {
		return menuCodeSet;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

}
