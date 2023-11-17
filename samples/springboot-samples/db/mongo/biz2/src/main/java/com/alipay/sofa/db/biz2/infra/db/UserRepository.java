package com.alipay.sofa.db.biz2.infra.db;

import com.alipay.sofa.db.biz2.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserRepository implements CRUDRepository<User, String> {

    @Autowired
    private MongoTemplate mongoTemplate;
    @Override
    public <S extends User> S save(S entity) {
        mongoTemplate.save(entity);
        return entity;
    }

    @Override
    public Optional<User> findById(String primaryKey) {
        return Optional.ofNullable(mongoTemplate.findById(primaryKey, User.class));
    }

    @Override
    public Iterable<User> findAll() {
        return mongoTemplate.findAll(User.class);
    }

    @Override
    public long count() {
        return mongoTemplate.count(new Query(), User.class);
    }

    @Override
    public void delete(User entity) {
        mongoTemplate.remove(entity);
    }

    @Override
    public boolean existsById(String primaryKey) {
        return mongoTemplate.exists(new Query(), User.class);
    }
}
