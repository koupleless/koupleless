package com.alipay.sofa.db.biz1.infra.db;

import com.alipay.sofa.db.biz1.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OrderRepository implements CRUDRepository<Order, String> {

    @Autowired
    private MongoTemplate mongoTemplate;
    @Override
    public <S extends Order> S save(S entity) {
        mongoTemplate.save(entity);
        return entity;
    }

    @Override
    public Optional<Order> findById(String primaryKey) {
        return Optional.ofNullable(mongoTemplate.findById(primaryKey, Order.class));
    }

    @Override
    public Iterable<Order> findAll() {
        return mongoTemplate.findAll(Order.class);
    }

    @Override
    public long count() {
        return mongoTemplate.count(new Query(), Order.class);
    }

    @Override
    public void delete(Order entity) {
        mongoTemplate.remove(entity);
    }

    @Override
    public boolean existsById(String primaryKey) {
        return mongoTemplate.exists(new Query(), Order.class);
    }
}
