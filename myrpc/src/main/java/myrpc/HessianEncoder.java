package myrpc;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class HessianEncoder extends MessageToByteEncoder<Object> {


	@Override
	protected void encode(ChannelHandlerContext ctx, Object in, ByteBuf out) throws Exception {
		byte[] data = HessianSerialize.serialize(in);
		out.writeInt(data.length);
		out.writeBytes(data);
	}


}
