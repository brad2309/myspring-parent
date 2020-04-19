package myrpc.server;

import myrpc.common.User;
import myspring.anno.Autowired;
import myspring.anno.Component;

@Component
public class UserService {
	
	@Autowired
	private UserDAO dao;

	public String getUser(Integer id){
		dao.selectById(id);
		User u = new User();
		u.setName("tom");
		u.setAge(108);
		return u.getName();
	}
	
}
