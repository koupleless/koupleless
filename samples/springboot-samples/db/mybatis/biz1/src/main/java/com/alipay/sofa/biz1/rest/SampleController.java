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
    private ApplicationContext applicationContext;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private SongMapper songMapper;

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
