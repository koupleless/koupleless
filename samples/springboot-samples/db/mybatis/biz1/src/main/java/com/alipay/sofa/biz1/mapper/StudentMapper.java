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
