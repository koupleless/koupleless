package com.alipay.sofa.biz1.mapper;

import com.alipay.sofa.biz1.model.Song;

import java.util.List;

public interface SongMapper {

    List<Song> getAll();

    Song getOne(Integer id);

    void insert(Song song);

    void update(Song song);

    void delete(Integer id);
}
