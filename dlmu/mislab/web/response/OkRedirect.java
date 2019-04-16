// By GuRui on 2014-12-19 上午1:03:47
package dlmu.mislab.web.response;

public class OkRedirect extends Response{
	private String url;

	public OkRedirect(){
		this(null);
	}
	
	public OkRedirect(String url){
		super(true);
		this.setUrl(url);
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
