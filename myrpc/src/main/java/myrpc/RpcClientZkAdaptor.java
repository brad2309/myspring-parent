package myrpc;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import myzk.ZkClient;

public class RpcClientZkAdaptor {
	
	public static Map<String, Set<String>> methodHostMap = new HashMap<String, Set<String>>();
	public static Map<String,RpcClient> hostClientMap = new HashMap<>();

	/**把本机需要的服务从zk获取提供方的地址并创建rpc连接*/
	public static void init(Set<Class<?>> allClass){
		List<String> list = new ArrayList<String>();//所有需要调用的远程服务
		for(Class<?> cls:allClass){
			if(cls.isAnnotationPresent(RpcService.class)&&ClassUtil.getImpl(cls,allClass)==null){
				for(Method m:cls.getMethods()){
					list.add(m.toString());
				}
			}
		}
		if(list.size()==0){
			return;
		}
		String rpc = ZkClient.getByDir("rpc");
		if(rpc==null||rpc.length()==0){
			return;
		}
		JSONObject jo = JSON.parseObject(rpc);
		Set<String> hosts = new HashSet<>();
		for(String key:jo.keySet()){
			String value = jo.getString(key);//key=rpc:localhost-3838;value=method1,method2
			System.out.println("RpcClientZkAdaptor:"+key+"="+value);
			String[] methods = value.split(",");
			String host = key.substring(4).replace("-", ":");
			for(String method:methods){
				if(list.contains(method)){
					Set<String> st = methodHostMap.get(method);
					if(st==null){
						st = new HashSet<>();
						methodHostMap.put(method, st);
					}
					st.add(host);
					hosts.add(host);
				}
			}
		}
		for(String host:hosts){
			RpcClient rc = new RpcClient(Integer.valueOf(host.split(":")[1]), host.split(":")[0]);
			rc.start(0);
			hostClientMap.put(host, rc);
		}
		System.out.println("RpcClientZkAdaptor.init:"+hosts);
	}
	
	
}
