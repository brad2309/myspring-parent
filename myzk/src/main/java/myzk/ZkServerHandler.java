package myzk;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ZkServerHandler extends SimpleChannelInboundHandler<ZkCommend>{
	
	
	private ThreadPoolExecutor pool = new ThreadPoolExecutor(16, 16, 600L, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(65536));

	protected void channelRead0(final ChannelHandlerContext ctx, final ZkCommend msg)throws Exception {
		System.out.println("server read:"+JSON.toJSONString(msg));
		pool.submit(new Runnable() {
			public void run() {
				invoke(msg,ctx.channel());
			}
		});
		
	}
	private void invoke(ZkCommend r,Channel channel) {
		ZkCommend resp = new ZkCommend(r.getRequestId(),r.getCommend());
		try{
			if(r.getCommend().equals("put")){
				JSONObject obj = JSON.parseObject(r.getBody());
				System.out.println("request put:"+obj.getString("value"));
				ZkService.put(
						channel.remoteAddress().toString(), 
						obj.getString("key"), 
						obj.getString("value"),
						obj.getBoolean("isTemp"));
			}else if(r.getCommend().equals("watch")){
				ZkService.addWatch(channel.remoteAddress().toString(), r.getBody());
			}else if(r.getCommend().equals("getByDir")){
				Map<String, String> map = ZkService.getByDir(r.getBody());
				resp.setBody(JSON.toJSONString(map));
			}
			System.out.println(JSON.toJSONString(resp));
		}catch (Exception e) {
			e.printStackTrace();
		}
		channel.writeAndFlush(resp);
	}
	
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println(ctx.channel().remoteAddress()+" active");
		ZkService.active(ctx.channel());
	}
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		System.out.println(ctx.channel().remoteAddress()+" inactive");
		ZkService.inactive(ctx.channel());
	}
	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		System.out.println(ctx.channel().remoteAddress()+" register");
	}
	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		System.out.println(ctx.channel().remoteAddress()+" unregister");
	}
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		System.out.println(ctx.channel().remoteAddress()+" add");
	}
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		System.out.println(ctx.channel().remoteAddress()+" readcomplete");
	}
	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		System.out.println(ctx.channel().remoteAddress()+" remove");
	}
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		System.out.println(ctx.channel().remoteAddress()+" exception:"+cause.getMessage());
		ctx.close();
	}
	

	
	
}
