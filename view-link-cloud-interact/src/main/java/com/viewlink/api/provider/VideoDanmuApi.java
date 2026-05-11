package com.viewlink.api.provider;

import com.viewlink.constants.Constants;
import com.viewlink.entity.po.VideoDanmu;
import com.viewlink.entity.query.VideoDanmuQuery;
import com.viewlink.entity.vo.PaginationResultVO;
import com.viewlink.entity.vo.ResponseVO;
import com.viewlink.mappers.VideoDanmuMapper;
import com.viewlink.service.VideoDanmuService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping(Constants.INNERAPI)
@Validated
public class VideoDanmuApi {

    @Resource
    private VideoDanmuService videoDanmuService;
    @Resource
    private VideoDanmuMapper<VideoDanmu,VideoDanmuQuery> videoDanmuMapper;
    /**
     * 管理后台加载弹幕
     * */
    @RequestMapping(Constants.INTERACT_ADMIN_REQUEST+"/loadDanmu")
    public PaginationResultVO<VideoDanmu> loadDanmu(Integer pageNo, String videoNameFuzzy) {
        VideoDanmuQuery videoDanmuQuery=new VideoDanmuQuery();
        videoDanmuQuery.setPageNo(pageNo);
        videoDanmuQuery.setOrderBy("danmu_id desc");
        videoDanmuQuery.setQueryVideoInfo(true);
        videoDanmuQuery.setVideoNameFuzzy(videoNameFuzzy);
        PaginationResultVO<VideoDanmu> resultVO = videoDanmuService.findListByPage(videoDanmuQuery);
        return resultVO;
    }

    /**
     * 管理后台删除弹幕
     * */
    @RequestMapping(Constants.INTERACT_ADMIN_REQUEST+"/delDanmu")
    public void delDanmu(@NotNull Integer danmuId){
        videoDanmuService.delDanmu(danmuId,null);
    }

    /**
     * 根据视频Id删除视频下的弹幕
     * */
    @RequestMapping(Constants.INTERACT_DANMU_REQUEST+"/delDanmuByVideoId")
    public void delDanmuByVideoId(@NotEmpty String videoId){
        VideoDanmuQuery videoDanmuQuery=new VideoDanmuQuery();
        videoDanmuQuery.setVideoId(videoId);
        videoDanmuMapper.deleteByParam(videoDanmuQuery);

    }
}
