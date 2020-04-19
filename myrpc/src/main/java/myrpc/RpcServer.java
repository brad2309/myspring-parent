package myrpc;

import java.util.HashMap;
import java.util.Map;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;


public class RpcServer {
	
	
	private final static int port = 3838;
    private static Map<String, Object> handlerMap = new HashMap<String, Object>();
	
	
	public static Map<String, Object> getHandlerMap() {
		return handlerMap;
	}
	public static void main(String[] args) {
		startServer();
	}
	public static void start(Integer delay){
		Thread t = new Thread(()->{
			try{
				Thread.sleep(delay);
			}catch (Exception e) {
				e.printStackTrace();
			}
			startServer();
		});
		t.start();
	}

	public static void startServer(){
		System.out.println("start netty server ...");
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try{
        	ServerBootstrap b = new ServerBootstrap();
        	b.group(bossGroup, workerGroup)
        	.channel(NioServerSocketChannel.class)
        	.childHandler(new ChannelInitializer<SocketChannel>() {
        		@Override
        		protected void initChannel(SocketChannel ch) throws Exception {
        			ChannelPipeline pipeline = ch.pipeline();
//        			pipeline.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
//        			pipeline.addLast(new StringDecoder());
//        			pipeline.addLast(new StringEncoder());
//        			pipeline.addLast(new NettyServerHandler());
        			pipeline
            		.addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0))
            		.addLast(new HessianDecoder())
            		.addLast(new HessianEncoder())
            		.addLast(new RpcServerHandler());
        		}
			})
        	.option(ChannelOption.SO_BACKLOG, 128)          // (5)
            .childOption(ChannelOption.SO_KEEPALIVE, true) // (6)
        	.bind(port).sync().channel().closeFuture().sync();
        }catch (Exception e) {
        	e.printStackTrace();
		}finally{
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
	

	
}
