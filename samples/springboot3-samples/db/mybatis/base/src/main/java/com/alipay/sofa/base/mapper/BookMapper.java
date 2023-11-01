package com.alipay.sofa.base.mapper;

import com.alipay.sofa.base.model.Book;

import java.util.List;
public interface BookMapper {

    List<Book> getAll();

    Book getOne(Integer id);

    void insert(Book book);

    void update(Book book);

    void delete(Integer id);
}
