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
package com.alipay.sofa.biz1.rest;

import com.alipay.sofa.biz1.mapper.SongMapper;
import com.alipay.sofa.biz1.mapper.StudentMapper;
import com.alipay.sofa.biz1.model.Song;
import com.alipay.sofa.biz1.model.Student;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SampleController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SampleController.class);

    @Autowired
    private ApplicationContext  applicationContext;

    @Autowired
    private StudentMapper       studentMapper;

    @Autowired
    private SongMapper          songMapper;

    @GetMapping("/hi")
    public String hello() {
        return String.format("hello to %s deploy", applicationContext.getId());
    }

    @GetMapping("/testmybatis")
    public List<Student> mybatis1() {
        Student student = new Student();
        student.setName("wangwu");
        student.setAge(20);
        student.setPhone("131xxxxxx");
        student.setGrade("five");
        studentMapper.insert(student);
        return studentMapper.getAll();
    }

    @GetMapping("/testmybatis1")
    public List<Song> mybatis2() {
        Song song = new Song();
        song.setName("March of the Volunteers");
        song.setAuthor("Er Nie");
        song.setAlbum("unknown");
        songMapper.insert(song);
        return songMapper.getAll();
    }

}
