# spring-telnet
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
启动后，自动监听8000端口，用telnet连接即可执行相应命令
## 连接console

tcp协议，直接telnet 8000端口，如果端口被占用，则往后推。非本机需要在TelnetServerHandler加ip白名单
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
比如该bean 有一个 public String getTestText(String cmd)  那么调用只需要输入 getTestText hello 回车键就可以得到函数的返回


## 异步

在请求的前面加入&则后面的命令会进入线程池异步执行。

## 其他

