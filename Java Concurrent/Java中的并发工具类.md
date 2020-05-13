---
title: Java中的并发工具类
date: {date}
tags: Java
categories: Java Concurrent
---
## 等待多线程完成的CountDownLatch
> [CountDownLatch理解一：与join的区别](https://blog.csdn.net/zhutulang/article/details/48504487)


CountDownLatch允许一个或多个线程等待其他线程完成操作。

```Java
public class JoinCountDownLatchTest {
    public static void main(String[] args) throws InterruptedException {
        Thread parser1 = new Thread(new Runnable() {
        @Override
        public void run() {
            }
        });
        Thread parser2 = new Thread(new Runnable() {
        @Override
        public void run() {
            System.out.println("parser2 finish");
            }
        });
    parser1.start();
    parser2.start();
    parser1.join();
    parser2.join();
    System.out.println("all parser finish");
    }
}
```
首先来看一下join，在当前线程中，如果调用某个thread的join方法，那么当前线程就会被阻塞，直到thread线程执行完毕，当前线程才能继续执行。join的原理是，不断的检查thread是否存活，如果存活，那么让当前线程一直wait，直到thread线程终止，线程的this.notifyAll 就会被调用。

```Java
public class CountDownLatchTest {
    staticCountDownLatch c = new CountDownLatch(2);
    public static void main(String[] args) throws InterruptedException {
    new Thread(new Runnable() {
        @Override
        public void run() {
            System.out.println(1);
            c.countDown();
            System.out.println(2);
            c.countDown();
        }
    }).start();
    c.await();
    System.out.println("3");
    }
}
```
CountDownLatch的构造函数接收一个int类型的参数作为计数器，如果你想等待N个点完成，这里就传入N。
当我们调用CountDownLatch的countDown方法时，N就会减1，CountDownLatch的await方法会阻塞当前线程，直到N变成零。由于countDown方法可以用在任何地方，所以这里说的N个点，可以是N个线程，也可以是1个线程里的N个执行步骤。用在多个线程时，只需要把这个CountDownLatch的引用传递到线程里即可。

> CountDownLatch中我们主要用到两个方法一个是await()方法，调用这个方法的线程会被阻塞，另外一个是countDown()方法，调用这个方法会使计数器减一，当计数器的值为0时，因调用await()方法被阻塞的线程会被唤醒，继续执行。

***
## 同步屏障CyclicBarrier
> [CyclicBarrier 相关整理](https://www.jianshu.com/p/9262361a1200)


CyclicBarrier的字面意思是可循环使用（Cyclic）的屏障（Barrier）。它要做的事情是，让一组线程到达一个屏障（也可以叫同步点）时被阻塞，直到最后一个线程到达屏障时，屏障才会开门，所有被屏障拦截的线程才会继续运行。
```Java
public class CyclicBarrierTest {
    // 自定义工作线程
    private static class Worker extends Thread {
        private CyclicBarrier cyclicBarrier;
        
        public Worker(CyclicBarrier cyclicBarrier) {
            this.cyclicBarrier = cyclicBarrier;
        }
        
        @Override
        public void run() {
            super.run();
            
            try {
                System.out.println(Thread.currentThread().getName() + "开始等待其他线程");
                cyclicBarrier.await();
                System.out.println(Thread.currentThread().getName() + "开始执行");
                // 工作线程开始处理，这里用Thread.sleep()来模拟业务处理
                Thread.sleep(1000);
                System.out.println(Thread.currentThread().getName() + "执行完毕");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
 
    public static void main(String[] args) {
        int threadCount = 3;
        CyclicBarrier cyclicBarrier = new CyclicBarrier(threadCount);
        
        for (int i = 0; i < threadCount; i++) {
            System.out.println("创建工作线程" + i);
            Worker worker = new Worker(cyclicBarrier);
            worker.start();
        }
    }}/**
创建工作线程0
创建工作线程1
Thread-0开始等待其他线程
创建工作线程2
Thread-1开始等待其他线程
Thread-2开始等待其他线程
Thread-2开始执行
Thread-0开始执行
Thread-1开始执行
Thread-1执行完毕
Thread-0执行完毕
Thread-2执行完毕
*/
```

* 总结
CyclicBarrier 的用途是让一组线程互相等待，直到全部到达某个公共屏障点才开始继续工作。CyclicBarrier 是可以重复利用的。在等待的只要有一个线程发生中断，则其它线程就会被唤醒继续正常运行。CyclicBarrier 指定的任务是进行 barrier 处最后一个线程来调用的，如果在执行这个任务发生异常时，则会传播到此线程，其它线程不受影响继续正常运行。
*  CyclicBarrier 和 CountDownLatch 的区别
1. CountDownLatch 是一个线程(或者多个)，等待另外 N 个线程完成某个事情之后才能执行；CyclicBarrier 是 N 个线程相互等待，任何一个线程完成之前，所有的线程都必须等待。
2. CountDownLatch 的计数器只能使用一次。而 CyclicBarrier 的计数器可以使用 reset() 方法重置；
CyclicBarrier 能处理更为复杂的业务场景，比如如果计算发生错误，可以重置计数器，并让线程们重新执行一次。
3. CountDownLatch 采用减计数方式；CyclicBarrier 采用加计数方式。