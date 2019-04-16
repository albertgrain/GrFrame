package dlmu.mislab.web.servlet;

import dlmu.mislab.common.IJson;


public class MySessionInfo implements IJson {
	private String username;
	private String tribeName;
	private Integer userId;
	private Boolean locked;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username == null ? null : username.trim();
	}

	public String getTribeName() {
		return tribeName;
	}

	public void setTribeName(String tribeName) {
		this.tribeName = tribeName == null ? null : tribeName.trim();
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Boolean getLocked() {
		return locked;
	}

	public void setLocked(Boolean locked) {
		this.locked = locked;
	}
}
