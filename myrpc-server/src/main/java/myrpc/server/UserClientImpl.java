package myrpc.server;

import myrpc.common.User;
import myrpc.common.UserClient;

public class UserClientImpl implements UserClient{
	
	@Override
	public User get(Integer id) {
		System.out.println("UserClientImpl"+id+" 888888888888");
		User u = new User();
		u.setId(id);
		u.setName("8888888888888");
		return u;
	}

	@Override
	public User add(User u) {
		u.setId(999);
		return u;
	}

}
