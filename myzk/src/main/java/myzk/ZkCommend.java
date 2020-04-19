package myzk;

import java.io.Serializable;

public class ZkCommend implements Serializable{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String commend;
	private String body;
    private String requestId;
    public ZkCommend() {
	}
    public ZkCommend(String requestId,String commend) {
    	this.commend = commend;
    	this.requestId = requestId;
	}
    public ZkCommend(String requestId,String commend,String body) {
    	this.commend = commend;
    	this.requestId = requestId;
    	this.body = body;
	}
	public String getCommend() {
		return commend;
	}
	public void setCommend(String commend) {
		this.commend = commend;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public String getRequestId() {
		return requestId;
	}
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
    
	

}
