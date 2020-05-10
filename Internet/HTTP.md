* **无状态协议**
  HTTP服务器并不保存关于客户的任何信息，所以我们说 HTTP 是一个无状态协议（stateless protocol）

* **怎么实现有状态呢？**
  Cookie 和 Session 保存会话状态，用 Session 来唯一标识用户，用 Cookie 当作用户通行证。

  session 有如用户信息档案表, 里面包含了用户的认证信息和登录状态等信息. 而 cookie 就是用户通行证。
1. **cookie**(客户端)
    
    * cookie 的4个技术组件：
    1. 在 HTTP 响应报文中的一个 cookie 首部行
    2. 在 HTTP 请求报文中的一个 cookie 首部行
    3. 在用户端系统中保留有一个 cookie 文件，并由用户的浏览器进行管理
4. 位于 Web 站点的一个后端数据库
    ![](https://github.com/Wayne-98/image/blob/master/Internet/Cookie.png?raw=true)


2. **session**(服务端)

Session 是服务器在和客户端建立连接时添加客户端连接标志，最终会在服务器软件（Apache、Tomcat、JBoss）转化为一个临时 Cookie 发送给给客户端，当客户端第一请求时服务器会检查是否携带了这个Session（临时 Cookie ），如果没有则会添加 Session，如果有就拿出这个 Session 来做相关操作

* session 安全性的保证
    * HttpOnly 属性
Cookie 的 HttpOnly 属性是 Cookie 的扩展功能，它使 JavaScript 脚本无法获得 Cookie。其主要目的为防止跨站脚本攻击（Cross-site scripting，XSS）对 Cookie 的信息窃取。
    * HTTPS传输
    

3. **token**

token 也称作令牌，由 uid + time + sign[+固定参数]

**token认证流程**
* token 的认证流程与 cookie 很相似，用户登录成功后服务器返回 Token 给客户端。
* 客户端收到数据后保存在客户端
* 再次访问服务器，将 token 放入 headers 中
* 服务器端采用 filter 过滤器校验。校验成功则返回请求数据，校验失败则返回错误码。

**区别和联系**
session 存储于服务器，可以理解为一个状态列表，拥有一个唯一识别符号 sessionId，通常存放于 cookie 中。服务器收到 cookie 后解析出 sessionId ，再去 session 列表中查找，才能找到相应 session ，依赖cookie。cookie 类似一个令牌，装有 sessionId ，存储在客户端，浏览器通常会自动添加。token 也类似一个令牌，无状态，用户信息都被加密到 token 中，服务器收到 token 后解密就可知道是哪个用户。需要开发者手动添加。

***
* **非持续性**连接和**持续性**连接
每个请求/响应对是经一个单独的TCP连接发送，还是所有的请求及响应经相同的TCP连接发送呢？
前者是非持续性连接，后者是持续性连接。（HTTP在其默认方式下使用持续性连接）
    * 非持续连接的缺点
        1. 每个连接，在客户和服务器中都要分配TCP的缓冲区和保持TCP变量，浪费资源
        2. 每个对象经受2倍的RTT(Round-Trip Time，往返时间),一个RTT用于创建TCP,一个RTT用于请求和接收一个对象。
***
* **HTTP报文格式**
* HTTP请求报文
```
GET /somedir/page.html HTTP/1.1
Host: www.someschool.edu
Connection: close
User-agent: Mozilla/5.0     //浏览器类型
Accept-language: fr         
```
HTTP请求报文的第一行叫做请求行（request line），其后继的行叫做首部行（header line）
1. **请求行**
* 方法字段：方法字段可以取不同的值：
    1. GET：实体体为空
    2. POST：实体体中包含的就是用户在表单字段中的输入值。
    * 当用户向搜索引擎提供搜索关键词时，使用post报文时，用户仍可以向服务器请求一个Web界面，但Web界面的特定内容依赖于用户在表单字段中输入的内容。  
    * GET方法也可以提交表单，在所请求的URL中包括输入的数据。
    `www.somesite.com/animalsearch?monkey&bananas`
    3. HEAD：HEAD方法类似与GET方法。当服务器收到一个使用HEAD方法的请求时，将会用一个HTTP报文进行响应，但是并不返回对象。应用程序开发者常用HEAD方法进行调试跟踪，用于确认 URL 的有效性以及资源更新的日期时间等。
    4. PUT：它允许用户上传对象到指定的Web服务器上指定的路径。由于自身不带验证机制，任何人都可以上传文件，因此存在安全性问题，一般不使用该方法。
    5. PATCH：修改资源，PATCH 允许部分修改。
    6. DELETE：允许用户或者应用程序删除Web服务器上的对象。并且同样不带验证机制。
    7. OPTIONS：查询指定的 URL 能够支持的方法。
    8. CONNECT：要求在与代理服务器通信时建立隧道
* URL字段：URL字段带有请求对象的标识
* HTTP版本字段
2. **首部行**
    Host：指明了对象所在的主机。（为什么已经有一条TCP连接存在了，还需要这个字段呢，因为Web代理高速缓存需要该首部行提供的信息）
    Connection：close首部行，该浏览器告诉服务器不要麻烦地使用持续连接，它要求服务器在发送完被请求的对象后就关闭这条连接。
    
* HTTP响应报文
```
HTTP/1.1 200 OK
Connection: close
Date: Tue, 18 Aug 2015 15:44:04 GMT
Server: Apache/2.2.3 (CentOS)
Last-Modified: Tue, 18 Aug 2015 15:11:04 GMT
Content-Length: 6821
Content-Type: text/html

(data data data data ....)
```
1. 初始状态行(status line)
    协议版本字段，状态码和相应状态信息
    * 200 OK ：请求成功，信息在返回的响应报文中
    * 301 Moved Permanently：请求的对象已经被永久转移了，新的URL定义在响应报文的Location：首部行中。客户软件将自动获取新的URL
    * 400 Bad Request：一个通用差错代码，指示该请求不能被服务器理解
    * 404 Not Found：被请求的文档不在服务器上
    * 505 HTTP Version Not Supported：服务器不支持请求报文使用的HTTP协议版本。
2. 6个首部行(header line)
    * Connection: close：首部行告诉客户，发送完报文后关闭该TCP连接
    * Date：首部行指示服务器产生并发送该响应报文的日期和时间。（不是指这个对象创建或者最后修改的时间，而是服务器从它的文件系统中检索到该对象，将该对象插入响应报文，并发送该响应报文的时间）
    * Server
    * Last-Modified：指示了这个对象创建或者最后修改的时间。(对既可能在本地客户也可能在网络缓存服务器上的对象缓存来说非常重要)
    * Length：指示了被发送对象中的字节数
    * Content-Type：
3. 实体体(entity body)
***
* Web缓存
Web缓存器(Web cache)也叫代理服务器(proxy server)，它是能够代表初始Web服务器来满足HTTP请求的网络实体。
    * 可以配置用户的浏览器，使得用户所有的HTTP请求首先指向Web缓存器。

1. 浏览器建立一个到web缓存器的TCP连接，并向web缓存器发送一个请求报文；
2. web缓存器检查本地是否存储了该对象的拷贝：如果有，向初始服务器发送一个条件GET请求报文（if-modified-since标记），检查本地拷贝是否是最新的：服务器会返回一个响应报文，如果是最新的，响应报文中不会包含请求对象(304 Not Modified)；如果不是最新的，响应报文中会包含请求对象，web缓存器更新其本地存储及相应的if-modified-since标记。如果没有，向初始服务器发送一个请求报文，收到请求后，更新本地存储。发送响应报文给客户端浏览器。

GET /fruit/kiwi.gif HTTP/1.1
Host: www.exotiquecuisine.com

HTTP/1.1 200 Ok
Date: Sat, 3 Oct 2015 15:39:29
Server: Apache/1.3.0 (Unix)
Last-Modified: Wed, 9 Sep 2015 09:23:24
Content-Type: image/gif
(data data data data ... ) 

GET /fruit/kiwi.gif HTTP/1.1
Host: www.exotiquecuisine.com
If-modified-since：Wed, 9 Sep 2015 09:23:24

HTTP/1.1 304 Not Modified
Date: Sat, 10 Oct 2015 15:39:29
Server: Apache/1.3.0 (Unix)
(empty entity body)

