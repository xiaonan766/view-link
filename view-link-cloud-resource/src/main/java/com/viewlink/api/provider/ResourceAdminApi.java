package com.viewlink.api.provider;

import com.viewlink.constants.Constants;
import com.viewlink.controller.FileController;
import com.viewlink.entity.config.AppConfig;
import com.viewlink.entity.enums.DateTimePatternEnum;
import com.viewlink.utils.DateUtil;
import com.viewlink.utils.FFmpegUtils;
import com.viewlink.utils.StringTools;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import static com.viewlink.constants.Constants.File_COVER;
import static com.viewlink.constants.Constants.File_FOLDER;


@RestController
@RequestMapping(Constants.INNERAPI+Constants.RESOURCE_ADMIN_REQUEST)
public class ResourceAdminApi {
    @Resource
    private AppConfig appConfig;
    @Resource
    private FFmpegUtils ffmpegUtils;
    @Resource
    private FileController fileController;

    /**
     * 上传图片
     * */
    @PostMapping("/uploadImage")
    public String uploadImage(@NotNull MultipartFile file, @NotNull Boolean createThumbnail) throws IOException {
        //根据月份创建文件夹
        //获取月份
        String month = DateUtil.format(new Date(), DateTimePatternEnum.YYYYMM.getPattern());
        //文件存放的临时路径，由静态资源指定目录+file/temp/month拼接而成
        String folder = appConfig.getProjectFolder() + File_FOLDER + File_COVER + month;
        //创建文件
        File folderFile = new File(folder);
        //判断路径是否存在
        if (!folderFile.exists()) {
            //如果文件不存在，创建文件夹
            folderFile.mkdirs();
        }
        //用随机字符串拼接文件名称避免重复
        //获取源文件的名称
        String fileName = file.getOriginalFilename();
        //获取源文件名称中“.”之后的字符串，即文件后缀名
        String fileSuffix = StringTools.getFileSuffix(fileName);
        //真实名称，由随机字符串和源文件名称拼接而成，避免发生文件名重复而造成文件覆盖
        String realFileName = StringTools.getRandomString(Constants.LENGTH_30) + fileSuffix;
        String filePath = folder + "/" + realFileName;
        //transferTo是复制file文件到指定位置(比如D盘下的某个位置),不然程序执行完,文件就会消失,程序运行时,临时存储在temp这个文件夹中
        file.transferTo(new File(filePath));
        //判断是否需要生成缩略图
        if (createThumbnail) {
            //生成缩略图
            ffmpegUtils.createImageThumbnail(filePath);
        }
        return File_COVER + month + "/" + realFileName;
    }

    /**
     * 获取资源
     * */
    @RequestMapping("/getResource")
    public void getResource(HttpServletResponse response, @NotNull String sourceName) throws IOException {
        fileController.getResource(response,sourceName);
    }

    /**
     * 获取视频资源
     * */
    @RequestMapping("/videoResource/{fileId}")
    public void videoResource(HttpServletResponse response, @PathVariable @NotEmpty String fileId){
        fileController.getVideoResource(response,fileId);
    }

    /**
     * 获取ts资源
     * */
    @RequestMapping("/videoResource/{fileId}/{ts}")
    public void videoResourceTs(HttpServletResponse response,@PathVariable @NotEmpty String fileId,@PathVariable @NotEmpty String ts){
        fileController.getVideoResourceTs(response,fileId,ts);
    }
}
