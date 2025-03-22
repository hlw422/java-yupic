package com.hlw.yupipictureend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PictureUploadRequest implements Serializable {

    /**
     * id
     */
    private Long id;


    /**
     * 简介
     */
    private String introduction;


    private String picName;



    /**
     * 分类
     */
    private String category;

    /**
     * 标签
     */
    private List<String> tags;

    private static final long serialVersionUID = 1L;

    private String fileUrl;



}

