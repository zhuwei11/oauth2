package com.zw.common.constant;

public class SysConstant {
	
	public static final String INVALID_CLIENT_GRANT = "VERIFY_CLIENTID_FAIL";
    public static final String INVALID_CLIENT_SECRET = "VERIFY_CLIENT_SECRET_FAIL";
    
	public static final String CACHE_NAME_OAUTH2_AUTHORIZATION_STORE = "oauth2.authorization.store";
	
	 /** access_token过期时间，单位秒 **/
	public static final int OAUTH2_ACCESS_TOKEN_EXPIRES_TIME = 3600;
	
	/** refresh_token过期时间，单位天 **/
	public static final int OAUTH2_REFRESH_TOKEN_EXPIRES_TIME = 30;
	
	public static final String SESSION_ID_KEY = "session_id_key";
	
	public static final String SESSION_AUTH_CODE_RANDOM_KEY = "session_auth_code_random_key";

}
