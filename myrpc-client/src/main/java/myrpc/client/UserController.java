package myrpc.client;

import java.lang.reflect.Method;

import myrpc.common.User;
import myrpc.common.UserClient;
import myspring.anno.Autowired;
import myspring.anno.Controller;
import myspring.anno.Request;
import myzk.ZkClient;

@Controller
public class UserController {

	@Autowired
	private UserClient userClient;
	
	@Request
	public String getByDir(String dir){
		String u = ZkClient.getByDir(dir);
		return "@"+u;
	}
	@Request
	public User getUser(Integer id){
		User u = userClient.get(id);
		return u;
	}
	@Request
	public User addUser(User u){
		u = userClient.add(u);
		return u;
	}
	
	@Request
	public String put(String dir,String key,String value){
		ZkClient.put(dir+":"+key, value, true);
		return "@ok";
	}
	
	public static void main(String[] args) {
		Class<?> cls = UserClient.class;
		Method[] ms = cls.getMethods();
		for(Method m:ms){
			System.out.println(m);
		}
	}
}
