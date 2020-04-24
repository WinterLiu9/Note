**线程池的优点：**
* **降低资源消耗**。通过重复利用已创建的线程降低线程创建和销毁造成的消耗
* **提高响应速度**。当任务到达时，任务可以不需要等到线程创建就能立即执行。
* **提高线程的可管理性**。线程是稀缺资源，如果无限制地创建，不仅会消耗系统资源，还会降低系统的稳定性，使用线程池可以进行统一分配、调优和监控。
***
## 线程池的实现原理
提交一个新任务到线程池时，**线程池的处理流程**：
1. 线程池判断核心线程池里的线程是否都在执行任务。如果不是，则创建一个新的工作线程来执行任务。如果核心线程池里的线程都在执行任务，则进入下个流程。
2. 线程池判断工作队列是否已经满。如果工作队列没有满，则将新提交的任务存储在这个工作队列里。如果工作队列满了，则进入下个流程
3. 线程池判断线程池的线程是否都处于工作状态。如果没有，则创建一个新的工作线程来执行任务。如果已经满了，则交给饱和策略来处理这个任务。
***
**ThreadPoolExecutor执行execute方法的4种情况：**
1. 如果当前运行的线程少于corePoolSize，则创建新线程来执行任务（注意，执行这一步骤需要获取全局锁）
2. 如果运行的线程等于或多于corePoolSize，则将任务加入BlockingQueue。
3. 如果无法将任务加入BlockingQueue（队列已满），则创建新的线程来处理任务（注意，执行这一步骤需要获取全局锁）。
4. 如果创建新线程将使当前运行的线程超出maximumPoolSize，任务将被拒绝，并调用RejectedExecutionHandler.rejectedExecution()方法。

![5648db12a8d970c8458136d773ae8152.png](en-resource://database/1289:1)
**总体设计思路**
* 是为了在执行execute()方法时，尽可能地避免获取全局锁（那将会是一个严重的可伸缩瓶颈）
* 在ThreadPoolExecutor完成预热之后（当前运行的线程数大于等于corePoolSize），几乎所有的execute()方法调用都是执行步骤2，而步骤2不需要获取全局锁。

**工作线程**
* 工作线程：线程池创建线程时，会将线程封装成工作线程Worker，Worker在执行完任务后，还会循环获取工作队列里的任务来执行
    * 线程池线程执行任务分两种情况：
        1. 在execute()方法中创建一个线程时，会让这个线程执行当前任务。
        2. 这个线程执行完上述任务时，会反复从BlockingQueue获取任务来执行
***
## 线程池的创建
```Java
new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime,milliseconds,runnableTaskQueue, handler);
```
1. corePoolSize（线程池的基本大小）
    如果调用了线程池的prestartAllCoreThreads()方法，线程池会提前创建并启动所有基本线程。
2. runnableTaskQueue（任务队列）
    * ArrayBlockingQueue：是一个基于数组结构的有界阻塞队列，此队列按FIFO（先进先出）原
则对元素进行排序。
    * **LinkedBlockingQueue**：一个基于链表结构的阻塞队列，此队列按FIFO排序元素，吞吐量通常要高于ArrayBlockingQueue。静态工厂方法Executors.newFixedThreadPool()使用了这个队列。
    * **SynchronousQueue**：一个不存储元素的阻塞队列。每个插入操作必须等到另一个线程调用移除操作，否则插入操作一直处于阻塞状态，吞吐量通常要高于Linked-BlockingQueue，静态工厂方法Executors.newCachedThreadPool使用了这个队列。
    * PriorityBlockingQueue：一个具有优先级的无限阻塞队列。
3. maximumPoolSize（线程池最大数量）：线程池允许创建的最大线程数。如果队列满了，并且已创建的线程数小于最大线程数，则线程池会再创建新的线程执行任务。值得注意的是，如果使用了无界的任务队列这个参数就没什么效果。
4. ThreadFactory：用于设置创建线程的工厂，可以通过线程工厂给每个创建出来的线程设置更有意义的名字。
5. RejectedExecutionHandler（饱和策略）：
    * AbortPolicy：直接抛出异常。（默认饱和处理策略）
    * CallerRunsPolicy：用“线程池正在运行的线程”来运行任务。(创建线程池的线程，一般是主线程)
    * DiscardOldestPolicy：当有任务添加到线程池被拒绝时，线程池会丢弃阻塞队列中末尾的任务，然后将被拒绝的任务添加到末尾
    * DiscardPolicy：不处理，丢弃掉。
    
    * 自定义饱和处理策略：
```Java
static class MyRejectedExecutionHandler implements RejectedExecutionHandler {
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        new Thread(r,"新线程"+new Random().nextInt(10)).start();
    }//让被拒绝的任务在一个新的线程中执行
}
```
6. keepAliveTime（线程活动保持时间）：线程池的工作线程空闲后，保持存活的时间。
    * 如果任务很多，并且每个任务执行的时间比较短，可以调大时间，提高线程的利用率。
7. TimeUnit（线程活动保持时间的单位）：可选的单位有天（DAYS）、小时（HOURS）、分钟（MINUTES）、毫秒（MILLISECONDS）、微秒（MICROSECONDS，千分之一毫秒）和纳秒（NANOSECONDS，千分之一微秒）。
***
## 向线程池提交任务
* **execute()** 方法用于提交不需要返回值的任务，所以无法判断任务是否被线程池执行成功
* **submit()** 方法用于提交需要返回值的任务。线程池会返回一个future类型的对象，通过这个future对象可以判断任务是否执行成功，并且可以通过future的get()方法来获取返回值，get()方法会阻塞当前线程直到任务完成，而使用get（long timeout，TimeUnit unit）方法则会阻塞当前线程一段时间后立即返回，这时候有可能任务没有执行完。
***
## 关闭线程池
* **shutdown或shutdownNow方法的执行原理：**
遍历线程池中的工作线程，然后逐个调用线程的interrupt方法来中断线程，所以无法响应中断的任务可能永远无法终止。
* **shutdown或shutdownNow方法的区别：**
1. shutdownNow首先将线程池的状态设置成STOP，然后尝试停止所有的正在执行或暂停任务的线程，并返回等待执行任务的列表。
2. shutdown只是将线程池的状态设置成SHUTDOWN状态，然后中断所有没有正在执行任务的线程。
***
## 线程池的状态

1. 当线程池创建后，初始为 running 状态
2. 调用 shutdown 方法后，处 shutdown 状态，此时不再接受新的任务，等待已有的任务执行完毕
3. 调用 shutdownnow 方法后，进入 stop 状态，不再接受新的任务，并且会尝试终止正在执行的任务。
4. 当处于 shotdown 或 stop 状态，并且所有工作线程已经销毁，任务缓存队列已清空，线程池被设为 terminated 状态。
## 合理使用线程池

* **高并发、任务执行时间短的业务怎样使用线程池？**
    高并发、任务执行时间短的业务，线程池线程数可以设置为CPU核数+1，减少线程上下文的切换
* **并发不高、任务执行时间长的业务怎样使用线程池？**
    1. 假如是业务时间长集中在IO操作上，也就是IO密集型的任务，因为IO操作并不占用CPU，所以不要让所有的CPU闲下来，可以适当加大线程池中的线程数目，让CPU处理更多的业务
    2. 假如是业务时间长集中在计算操作上，也就是计算密集型任务，这个就没办法了，和（1）一样吧，线程池中的线程数设置得少一些，减少线程上下文的切换
* **并发高、业务执行时间长的业务怎样使用线程池？**
并发高、业务执行时间长，解决这种类型任务的关键不在于线程池而在于整体架构的设计。
    1. 看看这些业务里面某些数据是否能做缓存是第一步，增加服务器是第二步，至于线程池的设置，设置参考
    2. 最后，业务执行时间长的问题，也可能需要分析一下，看看能不能使用中间件对任务进行拆分和解耦