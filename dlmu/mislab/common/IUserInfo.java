// By GuRui on 2015-2-5 上午12:42:44
package dlmu.mislab.common;

public interface IUserInfo{
	/**
	 * Get the unique id of user
	 * By GuRui on 2015-7-14 下午11:37:37
	 * @return
	 */
	public String getUserId();
	/**
	 * Get the username of user
	 * By GuRui on 2015-7-14 下午11:38:02
	 * @return
	 */
	public String getUsername();
	/**
	 * Get the role/group/privilege of user
	 * By GuRui on 2015-7-14 下午11:38:18
	 * @return
	 */
	public String getRole();
}
