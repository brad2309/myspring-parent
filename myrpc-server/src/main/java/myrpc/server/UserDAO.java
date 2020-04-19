package myrpc.server;

import myrpc.common.User;
import myspring.anno.DAO;
import myspring.anno.SQL;

@DAO
public interface UserDAO {

	@SQL("select * from user where id=:1")
	User selectById(Integer id);
	
}
