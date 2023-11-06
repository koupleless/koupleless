package com.alipay.sofa.db.biz1.infra.db;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

import java.util.Optional;

@NoRepositoryBean
public interface CRUDRepository<T, ID> extends Repository<T, ID> {
    <S extends T> S save(S entity);

    Optional<T> findById(ID primaryKey);

    Iterable<T> findAll();

    long count();

    void delete(T entity);

    boolean existsById(ID primaryKey);
}
