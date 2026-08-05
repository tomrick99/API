package org.example.file_api.tts.service;

//把byte[]里面的东西一坨二进制数据保存成文件
//是一个单独的接口 和讯飞无关

public interface AudioStorageService {
    String save(byte[] audioBytes);     //这个string是音频保存之后的路劲

}
