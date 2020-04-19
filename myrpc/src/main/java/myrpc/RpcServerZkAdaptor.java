package myrpc;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import myzk.ZkClient;

public class RpcServerZkAdaptor {

	/**把本机提供的服务信息注册到zk*/
	public static void init(Set<Class<?>> allClass){
		List<String> list = new ArrayList<String>();//所有提供调用的远程服务
		for(Class<?> cls:allClass){
			if(cls.isAnnotationPresent(RpcService.class)&&ClassUtil.getImpl(cls,allClass)!=null){
				for(Method m:cls.getMethods()){
					list.add(m.toString());
				}
			}
		}
		if(list.size()>0){
			ZkClient.start(0);
			try{
				Thread.sleep(1000);
			}catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("RpcServerZkAdaptor.put");
			String str = "";
			for(String s:list){
				str += s+",";
			}
			str = str.substring(0,str.length()-1);
			ZkClient.put("rpc:localhost-3838", str, true);
		}
	}
	
	
}
