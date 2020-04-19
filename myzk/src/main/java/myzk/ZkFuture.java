package myzk;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

public class ZkFuture {

    private Sync sync;
	private ZkCommend req;
	private ZkCommend res;
	
	public ZkFuture(ZkCommend req) {
        this.sync = new Sync();
		this.req = req;
	}
	
	public String get(){
		sync.acquire(-1);
		return res.getBody();
	}
	
	public void done(ZkCommend res){
		this.res = res;
		sync.release(1);
	}
	
	public ZkCommend getRes() {
		return res;
	}
	public ZkCommend getReq() {
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
