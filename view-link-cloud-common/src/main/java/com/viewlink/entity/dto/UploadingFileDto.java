package com.viewlink.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

//@JsonIgnoreProperties注解：转json过程中忽略类中不存在的字段
@JsonIgnoreProperties(ignoreUnknown = true)
public class UploadingFileDto implements Serializable {
    private static final long serialVersionUID=844272933084899283L;

    private String uploadId;//上传ID
    private String fileName;//文件名称
    private Integer chunkIndex;//分片索引
    private Integer chunks;//分片数
    private Long fileSize =0L;//文件大小
    private String filePath;//文件路径

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Integer getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(Integer chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Integer getChunks() {
        return chunks;
    }

    public void setChunks(Integer chunks) {
        this.chunks = chunks;
    }
}
