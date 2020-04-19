package myrpc;

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

public class RpcClient {
	
	

	private int port = 3838;
	private String host = "localhost";
	private Channel channel = null;
	public static RpcClient def = new RpcClient(3838, "localhost");
	
	public RpcClient(int port,String host){
		this.port = port;
		this.host = host;
	}
	public void start(Integer delay){
		Thread t = new Thread(()->{
			try{
				Thread.sleep(delay);
			}catch (Exception e) {
				e.printStackTrace();
			}
			connectServer();
		});
		t.start();
	}
	
	public void connectServer(){
		System.out.println("netty client1 init begin."+host+","+port);
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
            		.addLast(new RpcClientHandler());
            	}
			});
        	channel = b.connect(host, port).sync().channel();
        	channel.closeFuture().sync();
        }catch (Exception e) {
        	e.printStackTrace();
		}
	}
	
	public void send(RpcRequest nr){
		System.out.println("send:"+JSON.toJSONString(nr));
		channel.writeAndFlush(nr);
    	
	}
	@Override
	public String toString() {
		return host+":"+port;
	}
	
}

class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse>{
	
    public static ConcurrentHashMap<String, RpcFuture> futureMap = new ConcurrentHashMap<String, RpcFuture>();

	protected void channelRead0(ChannelHandlerContext ctx, RpcResponse nr) throws Exception {
		System.out.println(ctx.channel().remoteAddress()+"client read0:"+JSON.toJSONString(nr));
		
		RpcFuture f = futureMap.get(nr.getRequestId());
		futureMap.remove(nr.getRequestId());
		f.done(nr);
	}
	
	
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		System.out.println(ctx.channel().remoteAddress()+" exception:"+cause.getMessage());
	}
	
}
