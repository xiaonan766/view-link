package com.viewlink.api.consumer;

import com.viewlink.constants.Constants;
import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.io.IOException;


@FeignClient(name = Constants.SEVER_NAME_RESOURCE)
public interface ResourceClient {

    /**
     * 上传图片
     * */
    @PostMapping(value=Constants.INNERAPI+Constants.RESOURCE_ADMIN_REQUEST+"/uploadImage",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    String uploadImage(@RequestPart MultipartFile file, @RequestParam Boolean createThumbnail);

    /**
     * 获取资源
     * */
    @RequestMapping(value=Constants.INNERAPI+Constants.RESOURCE_ADMIN_REQUEST+"/getResource")
    Response getResource(@RequestParam String sourceName);

    /**
     * 获取视频资源
     * */
    @RequestMapping(value=Constants.INNERAPI+Constants.RESOURCE_ADMIN_REQUEST+"/videoResource/{fileId}")
    Response videoResource(@RequestParam String fileId);

    /**
     * 获取ts资源
     * */
    @RequestMapping(value=Constants.INNERAPI+Constants.RESOURCE_ADMIN_REQUEST+"/videoResource/{fileId}/{ts}")
    Response videoResourceTs(@RequestParam String fileId,@RequestParam String ts);
}
