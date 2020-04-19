package myzk;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class ZkServer {
	
	private final static int port = 1216;
	public static void main(String[] args) {
		startServer();
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
        			pipeline
            		.addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0))
            		.addLast(new HessianDecoder())
            		.addLast(new HessianEncoder())
            		.addLast(new ZkServerHandler());
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
