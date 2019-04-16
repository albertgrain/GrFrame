package dlmu.mislab.web.servlet;

import dlmu.mislab.common.IUserInfo;

public interface IAuthUserInfo extends IUserInfo {
	public int getAuthCode();
}
