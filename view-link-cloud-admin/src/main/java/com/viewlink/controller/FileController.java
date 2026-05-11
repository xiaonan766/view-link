package com.viewlink.controller;

import com.viewlink.api.consumer.ResourceClient;
import com.viewlink.constants.Constants;
import com.viewlink.entity.config.AppConfig;

import com.viewlink.entity.enums.DateTimePatternEnum;
import com.viewlink.entity.enums.ResponseCodeEnum;
import com.viewlink.entity.vo.ResponseVO;
import com.viewlink.exception.BusinessException;
import com.viewlink.utils.DateUtil;
import com.viewlink.utils.FFmpegUtils;
import com.viewlink.utils.StringTools;
import feign.Response;
import lombok.extern.slf4j.Slf4j;

import org.springframework.validation.annotation.Validated;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;


import static com.viewlink.constants.Constants.File_FOLDER;


@RestController
@RequestMapping("/file")
@Slf4j
@Validated
public class FileController extends ABaseController {

    @Resource
    private AppConfig appConfig;

    @Resource
    private ResourceClient resourceClient;

    /**
     * 上传图片
     * */
    @PostMapping("/uploadImage")
    public ResponseVO uploadImage(@NotNull MultipartFile file, @NotNull Boolean createThumbnail) throws IOException {
        return getSuccessResponseVO(resourceClient.uploadImage(file,createThumbnail));
    }

    /**
     * 获取资源
     * */
    @RequestMapping("/getResource")
    public void getResource(HttpServletResponse servletResponse, @NotEmpty String sourceName) throws IOException {
        Response response = resourceClient.getResource(sourceName);
        convertFileResponse2Stream(servletResponse,response);
    }
    /**
     * 获取资源
     * */
    @RequestMapping("/videoResource/{fileId}")
    public void videoResource(HttpServletResponse servletResponse, @PathVariable @NotEmpty String fileId) throws IOException {
        Response response = resourceClient.videoResource(fileId);
        convertFileResponse2Stream(servletResponse,response);
    }

    /**
     * 获取资源
     * */
    @RequestMapping("/videoResource/{fileId}/{ts}")
    public void videoResourceTs(HttpServletResponse servletResponse, @PathVariable @NotEmpty String fileId, @PathVariable @NotEmpty String ts) throws IOException {
        Response response = resourceClient.videoResourceTs(fileId, ts);
        convertFileResponse2Stream(servletResponse,response);
    }

    /*
    读取文件
    * */
    protected void readFile(HttpServletResponse response, String filePath) {
        File file = new File(appConfig.getProjectFolder() + File_FOLDER + filePath);
        //若想要读取的文件不存在，直接返回
        if (!file.exists()) {
            return;
        }
        //文件存在，通过输出流读取文件
        try (OutputStream out=response.getOutputStream(); FileInputStream in=new FileInputStream(file)) {
            byte[] byteData=new byte[1024];
            int len=0;
            //输入流in从资源中读取文件，输出流out写文件输出
            while ((len=in.read(byteData))!=-1){
                out.write(byteData,0,len);
            }
            out.flush();
        }catch (Exception e){
            //日志打印异常
            log.error("读取文件异常",e);
        }
    }
}
