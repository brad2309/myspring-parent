package myspring.init;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ServerStartListener implements ServletContextListener {

	public void contextDestroyed(ServletContextEvent arg0) {
		
	}

	public void contextInitialized(ServletContextEvent arg0) {
		try{
			BeanLoader.initMap();
			BeanLoader.initAutowire();
			MainServlet.initUriMap();
		}catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
