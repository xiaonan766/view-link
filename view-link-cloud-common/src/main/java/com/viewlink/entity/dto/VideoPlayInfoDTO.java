package com.viewlink.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class VideoPlayInfoDTO implements Serializable {

    private static final long serialVersionUID = 515461546124645L;;

    private String videoId;
    private String userId;
    private Integer fileIndex;

}
