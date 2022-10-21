# spring-telnet
# 用法

## 连接console

tcp协议，直接telnet 8000端口，如果端口被占用，则往后推。非本机需要在TelnetServerHandler加ip白名单

## 调用事先写好的console命令

创建一个@Qualifier("backConsole")的bean，然后该bean的所有方法可以直接通过 方法名 参数 参数 参数... 这样的命令去调用              
比如该bean 有一个 public String getTestText(String cmd)  那么调用只需要输入 getTestText hello 回车键就可以得到函数的返回

## 调用任何类的函数

输入 ```*[bean名称] [方法名] [参数] [参数] ...```可以执行任意一个容器bean的方法 输入 ```*[Class name] [方法名] [参数] [参数] ...``` 可以执行任意一个类的方法   
敏感函数勿用,bean的首字母可以不用小写，程序会自动转换首字母大小写的问题，但是连续的大写则不处理。

eg:

```
执行 telnet 127.0.0.1 8000
返回
hello~your connect ip is 127.0.0.1
try > echo hello
>
再次输入*lete.data.ad.common.utils.IPUtil find 183.6.71.80
返回
["CN"]
>

总体输出如下
hello~your connect ip is 127.0.0.1
try > echo hello
> *lete.data.ad.common.utils.IPUtil find 183.6.71.80
["CN"]
>
```

## 异步

在请求的前面加入&则后面的命令会进入线程池异步执行。

## 其他

