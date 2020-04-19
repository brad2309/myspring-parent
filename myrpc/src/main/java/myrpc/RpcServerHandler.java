package myrpc;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest>{
	
	
	private ThreadPoolExecutor pool = new ThreadPoolExecutor(16, 16, 600L, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(65536));

	protected void channelRead0(final ChannelHandlerContext ctx, final RpcRequest msg)throws Exception {
		System.out.println("server read:"+JSON.toJSONString(msg));
		pool.submit(new Runnable() {
			public void run() {
				invoke(msg,ctx.channel());
			}
		});
		
	}
	private void invoke(RpcRequest r,Channel channel) {
		try{
			Map<String, Object> map = RpcServer.getHandlerMap();
			Object obj = map.get(r.getInterfaceName());
			Method method = obj.getClass().getMethod(r.getMethodName(), r.getParameterTypes());
	        method.setAccessible(true);

	        RpcResponse nr = new RpcResponse();
	        nr.setRequestId(r.getRequestId());
	        try{
	        	Object res = method.invoke(obj, r.getArgs());
	        	nr.setResult(res);
		        nr.setSuccess(true);
	        }catch (Exception e) {
	        	e.printStackTrace();
	        	
			}
	        System.out.println("NettyServerHandler done."+channel.remoteAddress());
	        channel.writeAndFlush(nr);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println(ctx.channel().remoteAddress()+" active");
	}
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		System.out.println(ctx.channel().remoteAddress()+" inactive");
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
