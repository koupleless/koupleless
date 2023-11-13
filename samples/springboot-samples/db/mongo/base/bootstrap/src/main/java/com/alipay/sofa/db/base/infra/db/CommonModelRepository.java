package com.alipay.sofa.db.base.infra.db;

import com.alipay.sofa.db.base.model.CommonModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CommonModelRepository implements CRUDRepository<CommonModel, String> {

    @Autowired
    private MongoTemplate mongoTemplate;
    @Override
    public <S extends CommonModel> S save(S entity) {
        mongoTemplate.save(entity);
        return entity;
    }

    @Override
    public Optional<CommonModel> findById(String primaryKey) {
        return Optional.ofNullable(mongoTemplate.findById(primaryKey, CommonModel.class));
    }

    @Override
    public Iterable<CommonModel> findAll() {
        return mongoTemplate.findAll(CommonModel.class);
    }

    @Override
    public long count() {
        return mongoTemplate.count(new Query(), CommonModel.class);
    }

    @Override
    public void delete(CommonModel entity) {
        mongoTemplate.remove(entity);
    }

    @Override
    public boolean existsById(String primaryKey) {
        return mongoTemplate.exists(new Query(), CommonModel.class);
    }
}
