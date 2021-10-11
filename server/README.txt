问题:
1、注册 插入时间registTime字段没有时分秒
https://www.cnblogs.com/luoym/articles/5149400.html

rabbitMQ需要重新绑定

redis锁
redis缓存清除问题

12.19：
重新定义数据库
支持json和protobuf





1、集群netty的channel路由
用户登录时 将uuid和channel在本地做一个映射
然后再将uuid和rabbitMQ做一个映射 并存放在主服务器上

转发消息时 如果能在本地服务器找到uuid 直接转发
如果找不到 则将消息发到主服务器 由主服务器通过rabbitMQ转发到具体服务器


架构设计:
https://mp.weixin.qq.com/s?__biz=MzI1ODY0NjAwMA==&mid=2247483756&idx=1&sn=a8e3303bc573b1acaf9ef3862ef89bdd&chksm=ea044bf3dd73c2e5dcf2c10202c66d6143ec866205e9230f974fbc0b0be587926699230b6b18&scene=21#wechat_redirect
https://blog.csdn.net/jessechanrui/article/details/88399012



### 1、redis命令
redis-server  启动

redis-cli -h 127.0.0.1 -p 6379
flushall

### 2、mysql命令
mysql.server start 启动

### 3、rabbitmq
rabbitmq-server
后台地址:http://localhost:15672/#/

安装:https://www.rabbitmq.com/install-homebrew.html

### 4、zookeeper
zkServer start
zkServer stop
配置文件:/usr/local/etc/zookeeper/

https://blog.csdn.net/leiyu231/article/details/52292373?utm_medium=distribute.pc_relevant.none-task-blog-BlogCommendFromBaidu-1.control&depth_1-utm_source=distribute.pc_relevant.none-task-blog-BlogCommendFromBaidu-1.control
集群:https://blog.csdn.net/happywran/article/details/104712611/