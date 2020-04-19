项目介绍
用最简单代码以注解方式实现类似spring ioc/mvc/mybatis功能，实现rpc/zk基本功能
项目中提供example实现以上全部功能整合，并且调用成功
项目中仅依赖spring-core，因为需要用到工具类ParameterNameDiscoverer，没有依赖其他spring包

mvc:提供MainServlet转发所有请求、用ServerStartListener开机启动加载注解类，设置path和instance键值对
mybatis:用@DAO和@SQL注解在接口和方法上，用@Autowired调用，原理是动态代理机制
rpc：netty调用+hessian序列化；调用方式，服务接口加@RpcService注解，客户端调用加@Autowired直接调用
zk：netty+hessian；维护数据键值对、维护ip+port与key的对应关系、维护watch键值对等等；提供按目录存储和查询、watch通知、临时和永久存储等功能

启动步骤
1：启动ZkServer
2：单独启动myrpc-server
3：单独启动myrpc-client
4：访问myrpc-client/userController
