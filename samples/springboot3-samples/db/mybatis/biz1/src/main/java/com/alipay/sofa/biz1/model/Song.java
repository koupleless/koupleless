package com.alipay.sofa.biz1.model;

/**
 * @author: chaya.cy
 * @date: 2023/10/19 5:16 下午
 */
public class Song {

    private Integer id;

    private String name;

    private String author;

    private String album;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }
}
