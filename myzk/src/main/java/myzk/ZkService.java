package myzk;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSON;

import io.netty.channel.Channel;

public class ZkService {

	
	private static Map<String, String> dataMap = new HashMap<String, String>();
	private static Map<String, Set<String>> hostKeyMap = new HashMap<String, Set<String>>();
	private static Map<String, Set<String>> keyWatchMap = new HashMap<String, Set<String>>();
	private static Map<String,Channel> hostChannelMap = new HashMap<>();
	
	public static Map<String, String> getByDir(String dir){
		Map<String, String> map = new HashMap<>();
		for(String key:dataMap.keySet()){
			if(key.startsWith(dir+":")){
				map.put(key, dataMap.get(key));
			}
		}
		return map;
	}
	
	public static void active(Channel channel){
		String host = channel.remoteAddress().toString();
		hostChannelMap.put(host, channel);
	}
	public static void inactive(Channel channel){
		String host = channel.remoteAddress().toString();
		hostChannelMap.remove(host);
		if(!hostKeyMap.containsKey(host)){
			return;
		}
		Set<String> keys = hostKeyMap.get(host);
		if(keys==null||keys.size()==0){
			return;
		}
		for(String key:keys){
			dataMap.remove(key);
			keyWatchMap.remove(key);
			notify(key);
		}
		hostKeyMap.remove(host);
	}
	
	public static void addWatch(String host,String dir){
		Set<String> hosts = keyWatchMap.get(dir);
		if(hosts==null){
			hosts = new HashSet<>();
		}
		hosts.add(host);
		keyWatchMap.put(dir, hosts);
	}
	
	public static void put(String host,String key,String value,boolean isTemp){
		dataMap.put(key, value);
		if(isTemp){
			if(!hostKeyMap.containsKey(host)){
				Set<String> keys = new HashSet<>();
				hostKeyMap.put(host, keys);
			}
			Set<String> keys = hostKeyMap.get(host);
			keys.add(key);
		}
		notify(key);
	}
	
	
	public static void notify(String key){
		//dir:key
		String dir = key.substring(0,key.indexOf(":"));
		Set<String> hosts = keyWatchMap.get(dir);
		if(hosts!=null){
			for(String host:hosts){
				Channel chn = hostChannelMap.get(host);
				ZkCommend cmd = new ZkCommend();
				cmd.setCommend("notify");
				Map<String,Object> body = new HashMap<>();
				body.put("dir", dir);
				body.put("value", getByDir(dir));
				cmd.setBody(JSON.toJSONString(body));
				chn.writeAndFlush(cmd);
			}
		}
	}
	
}
