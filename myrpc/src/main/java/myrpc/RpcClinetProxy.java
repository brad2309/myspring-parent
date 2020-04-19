package myrpc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Set;
import java.util.UUID;


public class RpcClinetProxy<T> implements InvocationHandler{
	
	
	private Class<T> clazz;
	
	public RpcClinetProxy(Class<T> clazz) {
        this.clazz = clazz;
    }

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		RpcRequest r = new RpcRequest();
		r.setInterfaceName(clazz.getName());
		r.setMethodName(method.getName());
		r.setArgs(args);
		r.setRequestId(UUID.randomUUID().toString());
		r.setParameterTypes(method.getParameterTypes());
		r.setMethodStr(method.toString());
		return invoke(r);
	}
	
	public Object invoke(RpcRequest r) throws Exception{
		try{
			RpcFuture nf = new RpcFuture(r);
			RpcClientHandler.futureMap.put(r.getRequestId(), nf);
			Set<String> hosts = RpcClientZkAdaptor.methodHostMap.get(r.getMethodStr());
			System.out.println("hosts:"+hosts);
			if(hosts==null||hosts.size()==0){
				throw new RuntimeException("no host");
			}
			RpcClient rc = RpcClientZkAdaptor.hostClientMap.get(hosts.iterator().next());
			System.out.println(rc);
			rc.send(r);
			Object res = nf.get();
			return res;
		}catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T create(Class<T> interfaceClass) {
		return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new RpcClinetProxy<T>(interfaceClass)
        );
	}
	
}
