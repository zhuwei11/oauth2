package com.zw.common.model;

import com.jfinal.plugin.activerecord.ActiveRecordPlugin;

/**
 * Generated by JFinal, do not modify this file.
 * <pre>
 * Example:
 * public void configPlugin(Plugins me) {
 *     ActiveRecordPlugin arp = new ActiveRecordPlugin(...);
 *     _MappingKit.mapping(arp);
 *     me.add(arp);
 * }
 * </pre>
 */
public class _MappingKit {
	
	public static void mapping(ActiveRecordPlugin arp) {
		arp.addMapping("oauth_access_token", "id", OauthAccessToken.class);
		arp.addMapping("oauth_app", "id", OauthApp.class);
		arp.addMapping("oauth_refresh_token", "id", OauthRefreshToken.class);
		arp.addMapping("user_info", "id", UserInfo.class);
	}
}
