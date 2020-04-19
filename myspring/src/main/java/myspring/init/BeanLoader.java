package myspring.init;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import myrpc.RpcClientZkAdaptor;
import myrpc.RpcClinetProxy;
import myrpc.RpcServer;
import myrpc.RpcServerZkAdaptor;
import myrpc.RpcService;
import myspring.anno.Autowired;
import myspring.anno.Component;
import myspring.anno.Controller;
import myspring.anno.DAO;
import myspring.util.ClassUtil;
import myzk.ZkClient;

public class BeanLoader {

	public static Map<String,Class<?>> beanClassMap = new HashMap<String, Class<?>>();
	public static Map<Class<?>,Object> beanInstanceMap = new HashMap<Class<?>, Object>();
	public static Set<Class<?>> allClass;
	static Properties pro = new Properties();
	
	public static Object getBean(String beanName){
		Class<?> beanClass = beanClassMap.get(beanName);
		return beanInstanceMap.get(beanClass);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T  getBean(Class<T> cls){
		return (T)beanInstanceMap.get(cls);
	}
	
	public static void initMap() throws Exception{
		InputStream in = BeanLoader.class.getClassLoader().getResourceAsStream("application.properties");
		pro.load(in);
		in.close();
		allClass = ClassUtil.getClzFromPkg(pro.getProperty("package"));
		
		System.out.println("beanloader init begin.");
		for(Class<?> cls:allClass){
			if(cls.isAnnotationPresent(Component.class)||cls.isAnnotationPresent(Controller.class)){
				String className = cls.getName();
				String beanName = className.substring(className.lastIndexOf(".")+1);
				beanName = String.valueOf(beanName.charAt(0)).toLowerCase()+beanName.substring(1);
				beanClassMap.put(beanName, cls);
				Object instance = cls.newInstance();
				beanInstanceMap.put(cls, instance);
			}
		}
		initDAOMap();
		initRpcMap();
		System.out.println(beanClassMap);
		String rpcServer = pro.getProperty("rpc.server");
		ZkClient.start(0);
		Thread.sleep(100);
		if(rpcServer!=null&&rpcServer.equals("true")){
			RpcServerZkAdaptor.init(allClass);
			RpcServer.start(0);
		}else{
			Thread.sleep(1000);
			RpcClientZkAdaptor.init(allClass);
		}
				
	}
	public static void initRpcMap() throws Exception{
		System.out.println("rpcloader init begin.");
		for(Class<?> cls:allClass){
			if(cls.isAnnotationPresent(RpcService.class)){//接口
				Class<?> impl = ClassUtil.getImpl(cls,allClass);//实现类
				String className = cls.getName();
				String beanName = className.substring(className.lastIndexOf(".")+1);
				beanName = String.valueOf(beanName.charAt(0)).toLowerCase()+beanName.substring(1);
				BeanLoader.beanClassMap.put(beanName, cls);
				Object instance;
				if(impl==null){
					instance = RpcClinetProxy.create(cls);
				}else{
					instance = impl.newInstance();
				}
				BeanLoader.beanInstanceMap.put(cls, instance);
				RpcServer.getHandlerMap().put(cls.getName(), instance);
			}
		}
	}
	
	public static void initDAOMap(){
		System.out.println("daoloader init begin.");
		for(Class<?> cls:allClass){
			if(cls.isAnnotationPresent(DAO.class)){
				String className = cls.getName();
				String beanName = className.substring(className.lastIndexOf(".")+1);
				beanName = String.valueOf(beanName.charAt(0)).toLowerCase()+beanName.substring(1);
				BeanLoader.beanClassMap.put(beanName, cls);
				Object instance = DAOLoader.createInstance(cls);
				BeanLoader.beanInstanceMap.put(cls, instance);
			}
		}
	}
	public static void initAutowire() throws Exception{
		for(String beanName:beanClassMap.keySet()){
			Class<?> beanClass = beanClassMap.get(beanName);
			for(Field field:beanClass.getDeclaredFields()){
				if(field.isAnnotationPresent(Autowired.class)){
					field.setAccessible(true);
					field.set(beanInstanceMap.get(beanClass), beanInstanceMap.get(field.getType()));
				}
			}
		}
	}
	
}
