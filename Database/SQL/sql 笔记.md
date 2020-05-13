---
title: SQL 语句
date: {date}
tags: MySQL
categories: SQL
---

```sql
# 获取 Employee 表中第 n 高的薪水（Salary）
CREATE FUNCTION getNthHighestSalary(N INT) RETURNS INT
BEGIN
    set n = N - 1;
  RETURN (
      # Write your MySQL query statement below.
            select ifnull( # 如果第一个值为 null，则返回第二个值
            (
              select distinct salary 
              from employee 
              order by salary desc
               limit n, 1 # 从第 n 行开始，返回一行数据
            ), null)
  );
END
```



```sql
select Score, 
       dense_rank() over(order by score desc) as 'Rank'
from Scores;
```



```sql
select Email
from person
group by Email
having count(Email) > 1;
```



```sql
select 'Name' as Customers
from Customers a left join Orders b on a.id = b.CustomerId where b.id is null;
# 要判断一个数是否等于NULL只能用 IS NULL 或者 IS NOT NULL 来判断

select Name as Customers
from Customers
where id not in (
    select distinct CustomerId 
    from orders
);
```



```sql
delete p1
from (person p1 left join person p2 on p1.email = p2.email )
where p1.id > p2.id;
```

