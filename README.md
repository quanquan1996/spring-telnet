# spring-telnet
运行于spring容器的一个telnet服务端，使用netty通信，可以通过telnet输入命令去执行任意容器内实例的方法或者是不在容器内的类的方法
# 用途
1.测试阶段查看数据，方便调用方法，不用再额外写一些http接口去测试调用，或者是后门代码，节约时间，精简代码
2.线上定位问题，通过执行容器内方法，可以定位很多问题。或者是线上定时任务重跑场景，或者是后门场景，都是很方便的。
3.用做项目除http以外的通信协议。当需要内网通信时，可以用本项目自带的client工具rpc调用安装了本项目的服务，被调用方甚至不需要写代码
# 使用指南
## 配置
step 1,加入maven依赖
```maven
<dependency>
      <groupId>io.github.quanquan1996</groupId>
      <artifactId>spring-telnet</artifactId>
      <version>1.0-SNAPSHOT</version>
</dependency>
```
step2,启动类加 `@EnableTelnet` 注解
```java
import com.quanquan.telnet.telnet.EnableTelnet;

@EnableTelnet
@SpringBootApplication
public class AdTaskApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdTaskApplication.class, args);
    }

}
```
step3 启动后，自动监听8000端口，如果端口被占用，则往后推,可在启动日志处看到具体监听的端口。用telnet连接即可执行相应命令

step4 现在你可以telnet 服务8000端口，然后通过命令调用任何一个方法函数了
## 连接console

tcp协议，直接telnet 8000端口
## 调用任何类的函数

输入 ```*[bean名称] [方法名] [参数] [参数] ...```可以执行任意一个容器bean的方法 输入 ```*[Class name] [方法名] [参数] [参数] ...``` 可以执行任意一个类的方法   
敏感函数勿用,bean的首字母可以不用小写，程序会自动转换首字母大小写的问题，但是连续的大写则不处理。

如果想执行的类不在spring容器，则[bean名称]的参数使用全类名来代替 如 xxx.xxx.xxx.xxxUtil  
eg:

```
执行 telnet 127.0.0.1 8000
返回
hello~your connect ip is 127.0.0.1
try > echo hello
>
再次输入*com.xx.cxx.HelloWorld hello
返回
hello
>

比如想要调用一个bean叫webService,有一个test方法包含两个参数    
则输入 *webSerice test 123 1233 或者 *WebSerice test 123 1233

如果参数是实体类，则用json字符串来传输
```

## 调用事先写好的console命令

创建一个@Qualifier("backConsole")的bean，然后该bean的所有方法可以直接通过 方法名 参数 参数 参数... 这样的命令去调用              
比如该bean 有一个 public String getTestText(String cmd)  那么telnet调用只需要输入 getTestText hello 回车键就可以得到函数的返回


## 异步

在请求的前面加入&则后面的命令会进入线程池异步执行。
## 高级用法
### 把spring-telnet当做rpc框架
需要Server和Client端同时依赖spring-telnet，Client端只需要继承TelnetBaseService类，并配置服务端spring-telnet监听的端口，即可以通过命令的方式，实现rpc调用
eg:
继承类
```java
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TelnetApiService extends TelnetBaseService {
    @Value("${telnet.adApi.ip:127.0.0.1}")
    private String adApiIp;
    @Value("${telnet.adApi.port:8000}")
    private int adApiPort;

    @Override
    protected String getApiIp() {
        return adApiIp;
    }

    @Override
    protected int getApiPort() {
        return adApiPort;
    }

    @Override
    protected boolean isSync() {
        return false;
    }


}
```
使用方法：
```java
public class TestService{
    @Autowired
    TelnetApiService telnetApiService;
    //这里结尾需要加上换行符，标识命令输入完毕
    public static final String FLUSH_CMD = "*constantConfig flush\n";
    public void test(){
        //按配置的异步调用忽略结果
        telnetApiService.sendCmdToApi(FLUSH_CMD);
        //强制使用同步，同步获取调用结果
        String result = telnetApiService.sendCmdToApiSync(FLUSH_CMD);
    }
}
       
```
## 其他
### *前缀调用优先级
优先从spring容器匹配*后面的bean名字，匹配不到，则用*后面字符当做className去容器匹配，如果还匹配不到，则用Class for name
查找类，然后用默认的初始化方法初始化实例。    

比如容器内有一个productService的bean，类名为com.xx.xx.ProductService，有一个test(String a)的方法。
则可以使用 *productService test 123或者*ProductService test 123或者*com.xx.xx.ProductService test 123 
有个工具类com.xx.xx.xxUtil,使用*com.xx.xx.xxUtil test 12 ,因为优先级，在bean容器内找不到，则会使用Class.forName去找到类，然后用newInstance去实例化类，然后再调用具体方法并返回。
