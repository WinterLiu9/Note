# Java并发编程的艺术 第二章
## volatile的应用
### volatile的定义以及实现原理
* **有什么作用？**
Java线程内存模型确保所有线程看到这个变量的值是一致的。
* **volatile是如何来保证可见性的呢？**
    1. 将当前处理器缓存行的数据写回到系统内存。
   （Lock前缀指令会引起处理器缓存回写到内存。）
    2. 这个写回内存的操作会使在其他CPU里缓存了该内存地址的数据无效。
    （一个处理器的缓存回写到内存会导致其他处理器的缓存无效。）
* **缓存一致性协议**
每个处理器通过嗅探在总线上传播的数据来检查自己的缓存是不是过期了，这个写回内存的操作会使在其他CPU里缓存了该地址的数据无效。
***
## synchronized的实现原理与应用
* **实现的基础**
Java中的每一个对象都可以作为锁。
  
    1. 对于普通同步方法，锁是当前实例对象。
    2. 对于静态同步方法，锁是当前类的Class对象。
    3. 对于同步方法块，锁是Synchronized括号里配置的对象。
  
    

当一个线程访问同步代码块时，必须得到锁，退出或抛出异常时必须释放锁。

### Java对象头

* **synchronized用的锁是存在Java对象头里的**
    
    1. Mark Word
    存储对象的 HashCode、分代年龄和锁标记位
    2. Class Metadata Address
    存储到对象类型的指针
    3. Array Length
    如果是数组对象，则需要记录数组的长度
    
    ***
* **Mark Word的存储格式**
在32位系统上mark word长度为32bit，64位系统上长度为64bit。
为了能在有限的空间里存储下更多的数据，其存储格式是不固定的，在32位系统上各状态的格式如下：
![](https://github.com/Wayne-98/image/blob/master/Java%20Concurrent/MarkWord.png?raw=true)
![](https://github.com/Wayne-98/image/blob/master/Java%20Concurrent/MarkWord1.png?raw=true)

1. 当对象状态为偏向锁（biasable）时，mark word 存储的是偏向的线程 ID；
2. 当状态为轻量级锁（lightweight locked）时，mark word存储的是指向线程栈中Lock Record的指针；
3. 当状态为重量级锁（inflated）时，为指向堆中的monitor对象的指针。

***

* **Synchonized在JVM里的实现原理**
`javap -v 查看class文件对应的JVM字节码信息.`  
对于synchronized关键字而言，javac在编译时，会生成对应的monitorenter和monitorexit指令分别对应synchronized同步块的进入和退出
    * **synchronized 修饰一个同步块的时候**
一个monitorenter指令和两个monitorexit指令。
        * 原因是：为了保证抛异常的情况下也能释放锁，所以javac为同步代码块添加了一个隐式的try-finally，在finally中会调用monitorexit命令释放锁。
     * **synchronized 修饰一个方法的时候**
javac为其生成了一个ACC_SYNCHRONIZED关键字，在JVM进行方法调用时，发现调用的方法被ACC_SYNCHRONIZED修饰，则会先尝试获得锁。

### 锁的升级与对比
* **锁的状态**

    无锁状态-->偏向锁状态-->轻量级锁状态-->重量级锁状态
    锁可以升级但是不可以降级（为了提高锁获得和锁释放的效率）
***
*  **重量级锁**
重量级锁是我们常说的传统意义上的锁，其利用操作系统底层的同步机制去实现Java中的线程同步。
重量级锁的状态下，对象的mark word为指向一个堆中monitor对象的指针。
![](https://github.com/Wayne-98/image/blob/master/Java%20Concurrent/Monitor.png?raw=true)

* Contention List：竞争队列，所有请求锁的线程首先被放在这个竞争队列中；
* Entry List：Contention List中那些有资格成为候选资源的线程被移动到Entry List中；
* Wait Set：哪些调用wait方法被阻塞的线程被放置在这里；
* OnDeck：任意时刻，最多只有一个线程正在竞争锁资源，该线程被称为OnDeck；
* Owner：当前已经获取到所资源的线程被称为Owner；
* !Owner：当前释放锁的线程。
***
* 线程获取重量级锁的过程
    * 当一个线程尝试获得锁时，如果该锁已经被占用，则会将该线程封装成一个ObjectWaiter对象插入到Contention List的队列尾部，然后暂停当前线程；
    * 当持有锁的线程释放锁前，会将Contention List中的所有元素移动到Entry List中去，并唤醒Entry List的队首线程。
    * 如果一个线程在同步块中调用了Object#wait方法，会将该线程对应的ObjectWaiter从EntryList移除并加入到WaitSet中，然后释放锁；
    * 当wait的线程被notify之后，会将对应的ObjectWaiter从WaitSet移动到EntryList中。
    
    ***
* **轻量级锁**
    * **为什么引入轻量级锁？**
        在 Java 程序运行时，同步块中的代码都是不存在竞争的，不同的线程交替的执行同步块中的代码。这种情况下，用重量级锁是没必要的。因此 JVM 引入了轻量级锁的概念。

线程在执行同步块之前，JVM 会先在当前的线程的栈帧中创建一个 Lock Record，其包括一个用于存储对象头中的  mark word（官方称之为 Displaced Mark Word）以及一个指向对象的指针。下图右边的部分就是一个 Lock Record 。
![](https://github.com/Wayne-98/image/blob/master/Java%20Concurrent/LockRecord.png?raw=true)

* **加锁过程**
![a0496469cadecf59e405ae4615d9f1ec.png](en-resource://database/1160:1)
![f6fd1be7c49b1f03f2ebb2f2b2c51d84.png](en-resource://database/1162:1)

1. 在线程栈中创建一个Lock Record，将其obj（即上图的Object reference）字段指向锁对象。
2. 直接通过CAS指令将Lock Record的地址存储在对象头的mark word中，如果对象处于无锁状态则修改成功，代表该线程获得了轻量级锁。如果失败，进入到步骤3。
3. 如果是当前线程已经持有该锁了，代表这是一次锁重入。设置Lock Record第一部分（Displaced Mark Word）为null，起到了一个重入计数器的作用。然后结束。
4. 走到这一步说明发生了竞争，需要膨胀为重量级锁。
*  **解锁过程** 
1. 遍历线程栈,找到所有obj字段等于当前锁对象的Lock Record。
2. 如果Lock Record的Displaced Mark Word为null，代表这是一次重入，将obj设置为null后continue。
3. 如果Lock Record的Displaced Mark Word不为null，则利用CAS指令将对象头的mark word恢复成为Displaced Mark Word。如果成功，则continue，否则膨胀为重量级锁。
***

* **偏向锁**
    * **为什么引入偏向锁？**
偏向锁是在针对轻量级锁在没有竞争的情况下作出的优化,降低无竞争情况下获取锁的开销。
    * **对象创建**
        当JVM启用了偏向锁模式（1.6以上默认开启），当新创建一个对象的时候，如果该对象所属的class没有关闭偏向锁模式，那新创建对象的mark word将是可偏向状态，此时mark word中的thread id为0，表示未偏向任何线程，也叫做匿名偏向(anonymously biased)。
    * **加锁过程**
        1. 当该对象第一次被线程获得锁的时候，发现是匿名偏向状态，则会用CAS指令，将mark word中的thread id由0改成当前线程Id。如果成功，则代表获得了偏向锁，继续执行同步块中的代码。否则，将偏向锁撤销，升级为轻量级锁。
        2. 当被偏向的线程再次进入同步块时，发现锁对象偏向的就是当前线程，在通过一些额外的检查后，会往当前线程的栈中添加一条Displaced Mark Word为空的Lock Record中，然后继续执行同步块的代码，因为操纵的是线程私有的栈，因此不需要用到CAS指令；由此可见偏向锁模式下，当被偏向的线程再次尝试获得锁时，仅仅进行几个简单的操作就可以了，在这种情况下，synchronized关键字带来的性能开销基本可以忽略。
        3. 当其他线程进入同步块时，发现已经有偏向的线程了，则会进入到撤销偏向锁的逻辑里，一般来说，会在safepoint中去查看偏向的线程是否还存活，如果存活且还在同步块中则将锁升级为轻量级锁，原偏向的线程继续拥有锁，当前线程则走入到锁升级的逻辑里；如果偏向的线程已经不存活或者不在同步块中，则将对象头的mark word改为无锁状态（unlocked），之后再升级为轻量级锁。

由此可见，偏向锁升级的时机为：当锁已经发生偏向后，只要有另一个线程尝试获得偏向锁，则该偏向锁就会升级成轻量级锁。当然这个说法不绝对，因为还有批量重偏向这一机制。
***
* **偏向锁的撤销**

当有其他线程尝试获得锁时，是根据遍历偏向线程的lock record来确定该线程是否还在执行同步块中的代码。因此偏向锁的解锁很简单，仅仅将栈中的最近一条lock record的obj字段设置为null。需要注意的是，偏向锁的解锁步骤中并不会修改对象头中的thread id。   


偏向锁默认不是立即就启动的，在程序启动后，通常有几秒的延迟，可以通过命令
-XX:BiasedLockingStartupDelay=0来关闭延迟
***
* **批量重偏向与撤销**
    * **为什么要引入批量重偏向与撤销？**
从上文偏向锁的加锁解锁过程中可以看出，当只有一个线程反复进入同步块时，偏向锁带来的性能开销基本可以忽略，但是当有其他线程尝试获得锁时，就需要等到safe point时将偏向锁撤销为无锁状态或升级为轻量级/重量级锁。总之，偏向锁的撤销是有一定成本的，如果说运行时的场景本身存在多线程竞争的，那偏向锁的存在不仅不能提高性能，而且会导致性能下降。因此，JVM中增加了一种批量重偏向/撤销的机制。
    * **存在两种情况会导致偏向锁降低性能**
    1. 一个线程创建了大量对象并执行了初始的同步操作，之后在另一个线程中将这些对象作为锁进行之后的操作。这种case下，会导致大量的偏向锁撤销操作。
    2. 存在明显多线程竞争的场景下使用偏向锁是不合适的，例如生产者/消费者队列。
    
    

批量重偏向（bulk rebias）机制是为了解决第一种场景。批量撤销（bulk revoke）则是为了解决第二种场景。
    * **做法**
    1. 以class为单位，为每个class维护一个偏向锁撤销计数器，每一次该class的对象发生偏向撤销操作时，该计数器+1，当这个值达到重偏向阈值（默认20）时，JVM就认为该class的偏向锁有问题，因此会进行批量重偏向。
    2. 每个class对象会有一个对应的epoch字段，每个处于偏向锁状态对象的mark word中也有该字段，其初始值为创建该对象时，class中的epoch的值。每次发生批量重偏向时，就将该值+1，同时遍历JVM中所有线程的栈，找到该class所有正处于加锁状态的偏向锁，将其epoch字段改为新值。下次获得锁时，发现当前对象的epoch值和class的epoch不相等，那就算当前已经偏向了其他线程，也不会执行撤销操作，而是直接通过CAS操作将其mark word的Thread Id 改成当前线程Id。
    3. 当达到重偏向阈值后，假设该class计数器继续增长，当其达到批量撤销的阈值后（默认40），JVM就认为该class的使用场景存在多线程竞争，会标记该class为不可偏向，之后，对于该class的锁，直接走轻量级锁的逻辑。
***
* **End**
Java中的synchronized有偏向锁、轻量级锁、重量级锁三种形式，分别对应了锁只被一个线程持有、不同线程交替持有锁、多线程竞争锁三种情况。当条件不满足时，锁会按偏向锁->轻量级锁->重量级锁 的顺序升级。JVM种的锁也是能降级的，只不过条件很苛刻，不在我们讨论范围之内。该篇文章主要是对Java的synchronized做个基本介绍，后文会有更详细的分析。
***
## 原子操作的实现原理
* **处理器实现原子操作**
1. 通过总线锁保证原子性。总线锁就是使用处理器提供的一个LOCK＃信号，当一个处理器在总线上输出此信号时，其他处理器的请求将被阻塞住，那么该处理器可以独占共享内存。
2. 通过缓存锁定来保证原子性。缓存一致性协议

* **Java实现原子操作**
1. 循环CAS实现原子操作
CAS有3个操作数，内存值V，旧的预期值A，要修改的新值B。当且仅当预期值A和内存值V相同时，将内存值V修改为B，否则什么都不做。
```Java
private volatile int value;

public final int get() {
    return value;
}

public final int incrementAndGet() {
    for (;;) {
        int current = get();
        int next = current + 1;
        if (compareAndSet(current, next))
            return next;   
    }
}

public final boolean compareAndSet(int expect, int update) {
    return unsafe.compareAndSwapInt(this, valueOffset, expect, update);    
}
```
***
2. CAS实现原子操作的三大问题

* **ABA问题**
如果一个值原来是A，变成了B，又变成了A，那么使用CAS进行检查时会发现它的值没有发生变化，但是实际上却变化了。ABA问题的解决思路就是使用版本号。在变量前面追加上版本号，每次变量更新的时候把版本号加1，那么A→B→A就会变成1A→2B→3A
* **循环时间长开销大**
自旋CAS如果长时间不成功，会给CPU带来非常大的执行开销。如果JVM能支持处理器提供的pause指令，那么效率会有一定的提升。
    * pause指令有两个作用：
        1. 它可以延迟流水线执行指令（de-pipeline），使CPU不会消耗过多的执行资源，延迟的时间取决于具体实现的版本，在一些处理器上延迟时间是零。
        2. 它可以避免在退出循环的时候因内存顺序冲突（Memory Order Violation）而引起CPU流水线被清空（CPU Pipeline Flush），从而提高CPU的执行效率。
* **只能保证一个共享变量的原子操作**
    1. 用锁机制。
    2. 把多个共享变量合并成一个共享变量来操作。
***
3. 使用锁机制实现原子操作
除了偏向锁，JVM实现锁的方式都用了循环CAS，即当一个线程想进入同步块的时候使用循环CAS的方式来获取锁，当它退出同步块的时候使用循环CAS释放锁。