package com.reins.bookstore.repository;

import com.reins.bookstore.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
继承 JPA Repo中的接口
获得了操作数据库的方法
增/改：
    save(Book book)。如果 ID 已存在则更新，不存在则插入。
删：
    deleteById(Long id)，deleteAll()。
查：
    findById(Long id)：按主键查询（返回 Optional 防止空指针）。
    findAll()：获取表中所有数据。
    count()：统计行数
*/
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    /*
    param: Abstract Class, id category
     */
}

