/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
