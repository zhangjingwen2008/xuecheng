package com.xuecheng.test.fastdfs;

import org.csource.common.MyException;
import org.csource.fastdfs.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Administrator
 * @version 1.0
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestFastDFS {

    //测试上传
    @Test
    public void testUpload(){
        try {
            //加载fastdfs配置文件
            ClientGlobal.initByProperties("config/fastdfs-client.properties");
            //定义TrackerClient
            TrackerClient trackerClient = new TrackerClient();
            // 1.连接tracker
            TrackerServer trackerServer = trackerClient.getConnection();
            // 2.获取Storage
            StorageServer storageServer = trackerClient.getStoreStorage(trackerServer);
            //创建StorageClient
            StorageClient storageClient = new StorageClient(trackerServer, storageServer);
            // 3.向Storage服务器上传文件
            //本地文件路径
            String filePath = "D:\\Multimedia\\Picture\\1.png";
            // 4.上传成功后拿到文件id
            String[] fileId = storageClient.upload_file(filePath, "png", null);
            for (String s : fileId) {
                System.out.println(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //测试下载
    @Test
    public void testDownload(){
        try {
            ClientGlobal.initByProperties("config/fastdfs-client.properties");
            TrackerClient trackerClient = new TrackerClient();
            TrackerServer trackerServer = trackerClient.getConnection();
            StorageServer storageServer = trackerClient.getStoreStorage(trackerServer);
            StorageClient storageClient = new StorageClient(trackerServer, storageServer);
            // 3.下载文件
            //服务器文件id
            //String fileId = "group1/M00/00/00/wKhlBV1p1dOAYkzqAAXRqxzRErE886.png";
            String group="group1";
            String filePath = "M00/00/00/wKhlBV1p1dOAYkzqAAXRqxzRErE886.png";
            byte[] bytes = storageClient.download_file(group, filePath);
            FileOutputStream fileOutputStream = new FileOutputStream(new File("d:/test.png"));
            fileOutputStream.write(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //测试查询
    @Test
    public void testQuery(){
        try {
            ClientGlobal.initByProperties("config/fastdfs-client.properties");
            TrackerClient trackerClient = new TrackerClient();
            TrackerServer trackerServer = trackerClient.getConnection();
            StorageServer storageServer = trackerClient.getStoreStorage(trackerServer);
            StorageClient storageClient = new StorageClient(trackerServer, storageServer);
            String group="group1";
            String filePath = "M00/00/00/wKhlBV1p1dOAYkzqAAXRqxzRErE886.png";
            //查询文件
            FileInfo fileInfo = storageClient.query_file_info(group, filePath);
            System.out.println(fileInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
