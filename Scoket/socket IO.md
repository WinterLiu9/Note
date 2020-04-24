One basic concept of Linux (actually Unix) is the rule that everything in Unix/Linux is a file. Each process has a table of file descriptors that point to files, sockets, devices and other operating system objects.
	linux系统中，一切皆文件。，每一个进程都有一个FD表，指向了与该进程有关的文件，套接字，设备或者其他操作系统对象。
Typical system that works with many IO sources has an initializaion phase and then enter some kind of standby mode – wait for any client to send request and response it
典型系统中有许多IO资源具有初始化阶段，然后进入某种待机模式——等待任意一个客户端发送请求并对其进行响应。
Simple solution is to create a thread (or process) for each client , block on read until a request is sent and write a response. This is working ok with a small amount of clients but if we want to scale it to hundred of clients, creating a thread for each client is a bad idea
简单的解决方案是为每一个客户，创建一个线程，客户请求的时候进行响应，其他时候处于阻塞状态。

# IO Multiplexing

The solution is to use a kernel mechanism for polling over a set of file descriptors. There are 3 options you can use in Linux:
IO的多路复用解决方案是利于操作系统的内核机制来轮询一组文件描述符。

Multiplexing 指的其实是在单个线程通过记录跟踪每一个Sock(I/O流)的状态来同时管理多个I/O流. 
* select
* poll
* epoll

All the above methods serve the same idea, create a set of file descriptors , tell the kernel what would you like to do with each file descriptor (read, write, ..) and use one thread to block on one function call until at least one file descriptor requested operation available

