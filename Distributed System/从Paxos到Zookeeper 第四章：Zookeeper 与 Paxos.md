## Zookeeper基本概念

Zookeeper 是一个典型的分布式数据一致性的解决方案，分布式应用程序可以基于它实现诸如数据发布/订阅，负载均衡，命名服务，分布式协调/通知，集群管理，Master 选举，分布式锁和分布式队列等功能。

* 集群角色
  * Leader：为客户端提供读和写服务。
  * Follower：提供读服务
  * Observer：提供读服务，但是不参与 Leader 的选举过程，也不参与写操作的“过半写成功”策略，可以在不影响写性能的情况下提升集群的读性能。
* 会话
  * 客户端与 Zookeeper 的连接是一个 TCP 的长连接。ZK 对外提供服务的端口号为 2181。
* ZNode
  * ZNode 指的是数据模型中的数据单元，称之为数据节点。ZK 的所有数据存储在内存当中，数据模型是一棵树。
  * ZNode 可以分为持久性或临时性节点
* 版本
  * 每个ZNode，Zookeeper都会维护一个 Stat 的数据结构，Stat 记录了该 ZNode 的三个数据版本。version(当前ZNode 的版本)，cversion(当前ZNode子节点的版本)和aversion(当前ZNode的ACL版本)。
* Watcher
  * ZK 允许用户在指定节点上注册一些 Watcher，并且在一些特定事件触发的时候，ZK 服务端会将事件通知到感兴趣的客户端上去
* ACL
  * Access Control List 来进行权限控制
    1. CREATE：创建子节点的权限
    2. READ：获取节点数据和子节点列表的权限
    3. WRITE：更新节点数据的权限
    4. DELETE：删除子节点的权限
    5. ADMIN：设置节点 ACL 的权限

# ZAB 协议

Zookeeper Atomic  Broadcast（ZK原子消息广播协议）