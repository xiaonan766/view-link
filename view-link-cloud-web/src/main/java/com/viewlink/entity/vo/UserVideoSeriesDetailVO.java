package com.viewlink.entity.vo;

import com.viewlink.entity.po.UserVideoSeries;
import com.viewlink.entity.po.UserVideoSeriesVideo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserVideoSeriesDetailVO {

    private UserVideoSeries videoSeries;
    private List<UserVideoSeriesVideo> seriesVideoList;


}
