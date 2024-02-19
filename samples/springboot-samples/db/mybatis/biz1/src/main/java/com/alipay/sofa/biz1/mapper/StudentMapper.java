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
package com.alipay.sofa.biz1.mapper;

import com.alipay.sofa.biz1.model.Student;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author: yuanyuan
 * @date: 2023/10/18 8:49 下午
 */
public interface StudentMapper {

    @Select("SELECT * FROM student")
    List<Student> getAll();

    @Select("SELECT * FROM student WHERE id = #{id}")
    Student getOne(Integer id);

    @Insert("INSERT INTO student(name,phone,age,grade) VALUES(#{name}, #{phone}, #{age}, #{grade})")
    void insert(Student student);

    @Update("UPDATE student SET name=#{name},phone=#{phone},age=#{age},grade=#{grade} WHERE id =#{id}")
    void update(Student student);

    @Delete("DELETE FROM student WHERE id =#{id}")
    void delete(Integer id);
}
