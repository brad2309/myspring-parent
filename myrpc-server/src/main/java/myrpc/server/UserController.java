package myrpc.server;

import myrpc.common.User;
import myspring.anno.Autowired;
import myspring.anno.Controller;
import myspring.anno.Request;

@Controller
public class UserController {

	@Autowired
	private UserService userService;
	
	@Request
	public String get(Integer id){
		String u = userService.getUser(id);
		return u;
	}
	
}
