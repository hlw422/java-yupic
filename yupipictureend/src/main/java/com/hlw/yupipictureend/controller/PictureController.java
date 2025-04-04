package com.hlw.yupipictureend.controller;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.hlw.yupipictureend.annotation.AuthCheck;
import com.hlw.yupipictureend.common.BaseResponse;
import com.hlw.yupipictureend.common.DeleteRequest;
import com.hlw.yupipictureend.common.ResultUtils;
import com.hlw.yupipictureend.constant.UserConstant;
import com.hlw.yupipictureend.entity.Picture;
import com.hlw.yupipictureend.entity.User;
import com.hlw.yupipictureend.exception.BusinessException;
import com.hlw.yupipictureend.exception.ErrorCode;
import com.hlw.yupipictureend.exception.ThrowUtils;
import com.hlw.yupipictureend.model.dto.picture.*;
import com.hlw.yupipictureend.model.enums.PictureReviewStatusEnum;
import com.hlw.yupipictureend.service.PictureService;
import com.hlw.yupipictureend.service.UserService;
import com.hlw.yupipictureend.vo.PictureTagCategory;
import com.hlw.yupipictureend.vo.PictureVO;
import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/picture")
public class PictureController {
    @Resource
    private UserService userService;
    @Resource
    private PictureService pictureService;


    @Autowired
    private StringRedisTemplate redisTemplate;


    /**
     * Caffeine 缓存本地变量
     */
    private final Cache<String, String> LOCAL_CACHE =
            Caffeine.newBuilder().initialCapacity(1024)
                    .maximumSize(10000L)
                    // 缓存 5 分钟移除
                    .expireAfterWrite(5L, TimeUnit.MINUTES)
                    .build();

    @PostMapping("/upload")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PictureVO> uploadPicture(
            @RequestPart MultipartFile file,
            PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request
    ) {
        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.UploadPicture(file, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }
    @PostMapping("/upload/url")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PictureVO> uploadPictureByUrl(
            PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request
    ) {
        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.UploadPicture(pictureUploadRequest.getFileUrl(), pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }



    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest.getId() == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        Long userId = deleteRequest.getId();
        Picture oldPicture = pictureService.getById(deleteRequest.getId());
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 管理员可以删除任何图片
        if (userService.isAdmin(user)) {
            pictureService.removeById(deleteRequest.getId());
            return ResultUtils.success(true);
        }
        // 非管理员只能删除自己的图片
        if (Objects.equals(user.getId(), oldPicture.getUserId())) {
            pictureService.removeById(deleteRequest.getId());
            return ResultUtils.success(true);
        }
        throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
    }

    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updataPicture(@RequestBody PictureUploadRequest pictureUploadRequest, HttpServletRequest request) {
        if (pictureUploadRequest.getId() == null || pictureUploadRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureUploadRequest, picture);
        picture.setTags(JSONUtil.toJsonStr(pictureUploadRequest.getTags()));
        pictureService.validPicture(picture);


        Long id = pictureUploadRequest.getId();

        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 填充审核参数
        pictureService.fillReviewParams(picture, userService.getLoginUser(request));

        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取图片（仅管理员可用）
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Picture> getPictureById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(picture);
    }

    /**
     * 根据 id 获取图片（封装类）
     */
    @GetMapping("/get/vo")
    public BaseResponse<PictureVO> getPictureVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        PictureVO pictureVO = pictureService.getPictureVO(picture, request);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 分页获取图片列表（仅管理员可用）
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(picturePage);
    }

    /**
     * 分页获取图片列表（封装类）
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                             HttpServletRequest request) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        //普通用户只能查看已审核的图片
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());

        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(pictureService.getPicturVOPage(picturePage, request));
    }

    /**
     * 分页获取图片列表（封装类）
     */
    @PostMapping("/list/page/vo/cache")
    public BaseResponse<Page<PictureVO>> listPictureVOByPageWithCache(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                             HttpServletRequest request) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        //普通用户只能查看已审核的图片
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());

        //查询缓存中是否存在数据
        String queryKey = JSONUtil.toJsonStr(pictureQueryRequest);
        String haskey = DigestUtils.md5DigestAsHex(queryKey.getBytes());
        String redisKey = String.format("yupicture:listPictureVOByPage:%s", haskey);

        String localValue=LOCAL_CACHE.getIfPresent(redisKey);
        if(localValue!=null) {
            log.info("从本地缓存中获取数据");
            Page<PictureVO> pictureVOPage = JSONUtil.toBean(localValue, Page.class);
            return ResultUtils.success(pictureVOPage);
        }


        String redisValue = redisTemplate.opsForValue().get(redisKey);
        if (redisValue != null) {
            log.info("从缓存中获取数据");
            //存入本地缓存
            LOCAL_CACHE.put(redisKey, redisValue);

            Page<PictureVO> pictureVOPage = JSONUtil.toBean(redisValue, Page.class);
            return ResultUtils.success(pictureVOPage);
        }

        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));


        //缓存数据
        Page<PictureVO> pictureVOPage = pictureService.getPicturVOPage(picturePage, request);
        redisValue = JSONUtil.toJsonStr(pictureVOPage);
        LOCAL_CACHE.put(redisKey, redisValue);
        //设置随机过期时间 5-10分钟
        int expireTime = (int) (Math.random() * 5 + 5) * 60;
        redisTemplate.opsForValue().set(redisKey, redisValue, expireTime, java.util.concurrent.TimeUnit.SECONDS);

        return ResultUtils.success(pictureVOPage);
    }



    /**
     * 编辑图片（给用户使用）
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request) {
        if (pictureEditRequest.getId() == null || pictureEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureEditRequest, picture);
        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        picture.setEditTime(new Date());
        pictureService.validPicture(picture);

        Long id = pictureEditRequest.getId();
        User user = userService.getLoginUser(request);
        // 填充审核参数
        pictureService.fillReviewParams(picture, user);

        Picture oldPicture = pictureService.getById(pictureEditRequest.getId());
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 管理员可以删除任何图片
        if (userService.isAdmin(user)) {
            pictureService.updateById(picture);
            return ResultUtils.success(true);
        }
        // 非管理员只能删除自己的图片
        if (Objects.equals(user.getId(), oldPicture.getUserId())) {
            pictureService.updateById(picture);
            return ResultUtils.success(true);
        }
        throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
    }

    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意");
        List<String> categoryList = Arrays.asList("模板", "电商", "表情包", "素材", "海报");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);
    }

    /**
     * 审核图片
     *
     * @param pictureReviewRequest
     * @param request
     * @return
     */
    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> DoPictureReview(@RequestBody PictureReviewRequest pictureReviewRequest, HttpServletRequest request) {
        if (pictureReviewRequest.getId() == null || pictureReviewRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User LoginUser = userService.getLoginUser(request);
        pictureService.doPictureReview(pictureReviewRequest, LoginUser);
        return ResultUtils.success(true);
    }

    @PostMapping("/upload/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> uploadPictureByBatch(
            @RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(pictureUploadByBatchRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        int uploadCount = pictureService.uploadPictureByBatch(pictureUploadByBatchRequest, loginUser);
        return ResultUtils.success(uploadCount);
    }

}
