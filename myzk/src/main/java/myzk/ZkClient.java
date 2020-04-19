package myzk;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.fastjson.JSON;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class ZkClient {
	
	private static final int port = 1216;
	private static Channel channel = null;
	public static void main(String[] args) throws Exception{
		start(0);
		Thread.sleep(1000);
		watch("rpc");
	}
	
	public static void start(Integer delay){
		Thread t = new Thread(()->{
			try{
				Thread.sleep(delay);
			}catch (Exception e) {
				e.printStackTrace();
			}
			ZkClient.connectServer();
		});
		t.start();
	}
	
	public static void connectServer(){
		System.out.println("zk client init begin.");
		EventLoopGroup group = new NioEventLoopGroup();
        try{
        	Bootstrap b  = new Bootstrap()
            .group(group)
            .channel(NioSocketChannel.class)
            .handler(new ChannelInitializer<SocketChannel>() {
            	protected void initChannel(SocketChannel ch) throws Exception {
            		ChannelPipeline pipeline = ch.pipeline();
            		pipeline
            		.addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0))
            		.addLast(new HessianDecoder())
            		.addLast(new HessianEncoder())
            		.addLast(new ClientHandler());
            	}
			});
        	channel = b.connect("localhost", port).sync().channel();
        	channel.closeFuture().sync();
        }catch (Exception e) {
        	e.printStackTrace();
		}
	}
	public static void put(String key,String value,boolean isTemp){
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("key", key);
		map.put("value", value);
		map.put("isTemp", isTemp);
		System.out.println("put "+JSON.toJSONString(map));
		ZkCommend r = new ZkCommend(UUID.randomUUID().toString(), "put", JSON.toJSONString(map));
		ZkFuture nf = new ZkFuture(r);
		ClientHandler.futureMap.put(r.getRequestId(), nf);
		channel.writeAndFlush(r);
		nf.get();
	}
	public static String getByDir(String dir){
		ZkCommend r = new ZkCommend(UUID.randomUUID().toString(), "getByDir", dir);
		ZkFuture nf = new ZkFuture(r);
		ClientHandler.futureMap.put(r.getRequestId(), nf);
		channel.writeAndFlush(r);
		String res = nf.get();
		System.out.println("getByDir result "+res);
		return res;
	}
	public static String watch(String dir){
		ZkCommend r = new ZkCommend(UUID.randomUUID().toString(), "watch", dir);
		ZkFuture nf = new ZkFuture(r);
		ClientHandler.futureMap.put(r.getRequestId(), nf);
		channel.writeAndFlush(r);
		String res = nf.get();
		System.out.println("watch result "+res);
		return res;
	}
	
}

class ClientHandler extends SimpleChannelInboundHandler<ZkCommend>{
    public static ConcurrentHashMap<String, ZkFuture> futureMap = new ConcurrentHashMap<String, ZkFuture>();
	
	protected void channelRead0(ChannelHandlerContext ctx, ZkCommend nr) throws Exception {
		System.out.println(ctx.channel().remoteAddress()+"client read0:"+JSON.toJSONString(nr));
		ZkFuture f = futureMap.get(nr.getRequestId());
		futureMap.remove(nr.getRequestId());
		f.done(nr);
	}
	
	
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		System.out.println(ctx.channel().remoteAddress()+" exception:"+cause.getMessage());
		cause.printStackTrace();
	}
}
