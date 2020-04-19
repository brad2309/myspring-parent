package myspring.init;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import myspring.anno.SQL;

/**创建DAO代理类*/
public class DAOLoader {

	private static DAOHandlerImpl handlerImplInstance = new DAOHandlerImpl();
	
	
	/**又beanName创建代理类*/
	public static Object createInstance(Class<?> cls){
		if(BeanLoader.getBean(cls)!=null){
			return BeanLoader.getBean(cls);
		}
		Object daoProxyBean = Proxy.newProxyInstance(cls.getClassLoader(), new Class<?>[]{cls}, handlerImplInstance);
		BeanLoader.beanInstanceMap.put(cls, daoProxyBean);
		return daoProxyBean;
	}
}
class DAOHandlerImpl implements InvocationHandler{
	
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		String sql = method.getAnnotation(SQL.class).value();
		sql = sql.replace(":1", "'"+args[0]+"'");
		System.out.println("----------------execute sql:"+sql);
		return null;
	}
	
	
	
}
