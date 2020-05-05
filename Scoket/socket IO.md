One basic concept of Linux (actually Unix) is the rule that everything in Unix/Linux is a file. Each process has a table of file descriptors that point to files, sockets, devices and other operating system objects.
	linux系统中，一切皆文件。，每一个进程都有一个FD表，指向了与该进程有关的文件，套接字，设备或者其他操作系统对象。
Typical system that works with many IO sources has an initializaion phase and then enter some kind of standby mode – wait for any client to send request and response it
典型系统中有许多IO资源具有初始化阶段，然后进入某种待机模式——等待任意一个客户端发送请求并对其进行响应。
Simple solution is to create a thread (or process) for each client , block on read until a request is sent and write a response. This is working ok with a small amount of clients but if we want to scale it to hundred of clients, creating a thread for each client is a bad idea
简单的解决方案是为每一个客户，创建一个线程，客户请求的时候进行响应，其他时候处于阻塞状态。

***

sfd = socket();	// socket 系统调用 得到一个文件描述符代表这个socket

bind(sfd...)	// 绑定到一个端口号上

listen(sfd) 	// 开启监听状态

accept(sfd) 	// 接收客户端的连接 (阻塞) 

read recvfrom 

调用recv 的时候，操作系统会把该进程阻塞。当网络数据到达网卡的时候，网卡会把数据放到内存，然后中断通知CPU，CPU 执行中断程序，把网络数据写入到对应的socket的接受缓冲区中(端口号)，在唤醒阻塞的进程来读数据。

* 如何如何同时监视多个socket的数据？

每次有一个 client 新的连接，该线程就 clone 一个新的线程来执行。

***

# IO Multiplexing

The solution is to use a kernel mechanism for polling over a set of file descriptors. There are 3 options you can use in Linux:
IO的多路复用解决方案是利于操作系统的内核机制来轮询一组文件描述符。

Multiplexing 指的其实是在单个线程通过记录跟踪每一个Sock(I/O流)的状态来同时管理多个I/O流. 
* select
* poll
* epoll

All the above methods serve the same idea, create a set of file descriptors , tell the kernel what would you like to do with each file descriptor (read, write, ..) and use one thread to block on one function call until at least one file descriptor requested operation available



服务端需要管理多个客户端连接，而recv只能监视单个socket，这种矛盾下，人们开始寻找监视多个socket的方法。



1. **select** : 假如能够预先传入一个socket列表，**如果列表中的socket都没有数据，挂起进程，直到有一个socket收到数据，唤醒进程**。

   * 用法：先准备一个数组（下面代码中的fds），让fds存放着所有需要监视的socket。然后调用select，如果fds中的所有socket都没有数据，select会阻塞，直到有一个socket接收到数据，select返回，唤醒进程。用户可以遍历fds，通过FD_ISSET判断具体哪个socket收到数据，然后做出处理。

   ```c
   int s = socket(AF_INET, SOCK_STREAM, 0);  
   bind(s, ...)
   listen(s, ...)
   
   int fds[] =  存放需要监听的socket
   
   while(1){
       int n = select(..., fds, ...)
       for(int i=0; i < fds.count; i++){
           if(FD_ISSET(fds[i], ...)){
               //fds[i]的数据处理
           }
       }
   }
   ```

   	* **select的流程**：select的实现思路很直接。假如程序同时监视如下图的sock1、sock2和sock3三个socket，那么在调用select之后，操作系统把进程A分别加入这三个socket的等待队列中。

   ![]()

   

[如果这篇文章说不清epoll的本质，那就过来掐死我吧！ （1）](https://zhuanlan.zhihu.com/p/63179839)