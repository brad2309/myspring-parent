package myrpc;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

public class RpcFuture {

    private Sync sync;
	private RpcRequest req;
	private RpcResponse res;
	
	public RpcFuture(RpcRequest req) {
        this.sync = new Sync();
		this.req = req;
	}
	
	public Object get(){
		sync.acquire(-1);
		return res.getResult();
	}
	
	public void done(RpcResponse res){
		this.res = res;
		sync.release(1);
	}
	
	public RpcResponse getRes() {
		return res;
	}
	public RpcRequest getReq() {
		return req;
	}
	
	static class Sync extends AbstractQueuedSynchronizer {

        private static final long serialVersionUID = 1L;

        protected boolean tryAcquire(int acquires) {
            return getState() == 1 ? true : false;
        }

        protected boolean tryRelease(int releases) {
            if (getState() == 0) {
                if (compareAndSetState(0, 1)) {
                    return true;
                }
            }
            return false;
        }

    }
	
}
