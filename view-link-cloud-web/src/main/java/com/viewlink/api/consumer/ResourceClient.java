package com.viewlink.api.consumer;

import com.viewlink.constants.Constants;
import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(name = Constants.SEVER_NAME_RESOURCE)
public interface ResourceClient {

    @RequestMapping("/downloadVideo/{fileId}")
    Response downloadVideo(@PathVariable String fileId);
}
