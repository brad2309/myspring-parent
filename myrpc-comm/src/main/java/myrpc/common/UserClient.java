package myrpc.common;

import myrpc.RpcService;

@RpcService
public interface UserClient {

	User get(Integer id);
	User add(User u);
}
