package myspring.init;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;

import com.alibaba.fastjson.JSON;

import myspring.anno.Controller;
import myspring.anno.Request;

public class MainServlet extends HttpServlet{
	

	private static final long serialVersionUID = 1L;
	
	private static Map<String,Method> uriMethodMap = new HashMap<String, Method>();//路径对应Method
	private static Map<String,Object> uriInstanceMap = new HashMap<String, Object>();//路径对应类的实例
	private static Map<Method,String[]> methodParameterNameMap = new HashMap<Method, String[]>();//路径的Method对应其参数名
	
	
	public static void initUriMap() throws Exception{
		
		for(Class<?> actionClass:BeanLoader.allClass){
			if(!actionClass.isAnnotationPresent(Controller.class)){
				continue;
			}
			String prefix = actionClass.getSimpleName();
			if(!prefix.endsWith("Controller")){
				throw new RuntimeException("illegal controller class name:"+prefix);
			}
			prefix = (char)(prefix.charAt(0)+32)+prefix.substring(1,prefix.length()-10);
			Object instance = BeanLoader.getBean(actionClass);
			for(Method m:actionClass.getDeclaredMethods()){
				if(m.isAnnotationPresent(Request.class)){
					uriMethodMap.put("/"+prefix+"/"+m.getName(), m);
					uriInstanceMap.put("/"+prefix+"/"+m.getName(), instance);
					ParameterNameDiscoverer pn = new LocalVariableTableParameterNameDiscoverer();
					methodParameterNameMap.put(m, pn.getParameterNames(m));
				}
			}
		}
	}
	
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String uri = req.getRequestURI().substring(req.getContextPath().length());
		if(uri.endsWith(".html")||uri.endsWith(".jpg")){//静态资源由默认servlet加载
			getServletContext().getNamedDispatcher("default").forward(req, resp);
			return;
		}
		Method m = uriMethodMap.get(uri);
		if(m==null){//访问地址找不到
			req.getRequestDispatcher("/404.jsp").forward(req, resp);
			return;
		}
		Object[] args;
		try{
			args = setParamters(m, req, resp);//设置方法参数
		}catch (Exception e) {
			req.getRequestDispatcher("/400.jsp").forward(req, resp);
			return;
		}
		
		Object result;
		try {
			result = m.invoke(uriInstanceMap.get(uri), args);//调用Action
		}catch (Exception e) {
			e.printStackTrace();
			req.getRequestDispatcher("/500.jsp").forward(req, resp);
			return;
		}
		//处理返回
		Class<?> returnType = m.getReturnType();
		if(returnType.equals(String.class)){
			String resultStr = (String)result;
			if(resultStr!=null&&resultStr.length()>0&&resultStr.charAt(0)=='@'){//直接返回
				resp.getWriter().print(resultStr.substring(1));
			}else{//返回页面
				req.getRequestDispatcher((String)result+".jsp").forward(req, resp);
			}
		}else if(returnType.equals(void.class)){//无返回
			return;
		}else{//返回对象转为json
			resp.getWriter().print(JSON.toJSONString(result));
		}
		
	}
	
	/**设置方法参数*/
	private Object[] setParamters(Method m,HttpServletRequest req,HttpServletResponse resp) throws Exception{
		Class<?>[] paramTypes = m.getParameterTypes();//方法的参数类型
		Object[] args = new Object[paramTypes.length];
		String[] paramNames = methodParameterNameMap.get(m);//方法的各个参数的参数名
		String val;
		for(int i=0;i<paramTypes.length;i++){//方法的第几个参数
			Class<?> c = paramTypes[i];
			if(c.equals(HttpServletRequest.class)){
				args[i] = req;
			}else if(c.equals(HttpServletResponse.class)){
				args[i] = resp;
			}else if(c.equals(String.class)){
				args[i] = req.getParameter(paramNames[i]);
			}else if(c.equals(Integer.class)||c.equals(int.class)){
				val = req.getParameter(paramNames[i]);
				if(val!=null&&val.length()>0){
					if(!val.matches("\\d+")){
						throw new RuntimeException("400");
					}
					args[i] = Integer.parseInt(val);
				}else if(c.equals(int.class)){
					args[i] = 0;
				}
			}else{
				Object o = setParameters(c, req);//方法参数为对象，进行属性注入
				args[i] = o;
			}//TODO
			
		}
		return args;
	}
	
	private Object setParameters(Class<?> c,HttpServletRequest req) throws Exception{
		Enumeration<String> enums = req.getParameterNames();
		Object instance = c.newInstance();
		while(enums.hasMoreElements()){//遍历请求参数
			String key = enums.nextElement();
			String value = req.getParameter(key);
			if(value==null||value.length()==0){//参数值为空不处理
				continue;
			}
			Field f = null;//请求参数对应的属性名称
			for(Field e:c.getDeclaredFields()){
				if(e.getName().equals(key)){
					f = e;
					break;
				}
			}
			
			if(f==null){//没有这个属性
				continue;
			}
			String fieldName = f.getName();
			Class<?> fieldType = f.getType();
			//找到setter方法
			Method m = c.getDeclaredMethod("set"+(char)(fieldName.charAt(0)-32)+fieldName.substring(1), f.getType());
			Object param = null;
			if(fieldType.equals(String.class)){
				param = value;
			}else if(fieldType.equals(Integer.class)){
				if(value.matches("\\d+")){
					param = Integer.parseInt(value);
				}
			}//TODO
			m.invoke(instance, param);//setter方法注入属性值
		}
		return instance;
	}

}
