package com.example.server.controller;

import com.example.server.entity.FileInfoEntity;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import user.BaseEntity;
import utils.L;
import utils.UUIDUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = {"/file"})
public class FileController {

    @PostMapping("/upload")
    public BaseEntity<List<FileInfoEntity>> upload(@RequestParam("file") MultipartFile[] files, @RequestParam("param") String param, HttpServletRequest request) {
        if (ArrayUtils.isEmpty(files))
            return BaseEntity.fail();
        L.p("upload param==>" + param);
        List<FileInfoEntity> fileInfoList = new ArrayList<>();

        File folder = new File("./dir");
        if (!folder.exists())
            folder.mkdirs();
        try {
            for (int i = 0; i < files.length; i++) {
                MultipartFile file = files[i];
                String fn = file.getOriginalFilename();
                String suffix = fn.contains(".") ? fn.substring(fn.lastIndexOf(".")) : "";
                String fileNameNew = UUIDUtil.getUid() + suffix;
                File newFile = new File(folder, fileNameNew);
                file.transferTo(newFile);
                String url = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/" + newFile.getAbsolutePath() + "/" + fileNameNew;
                FileInfoEntity fie = new FileInfoEntity();
                fie.url = url;
                fileInfoList.add(fie);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return BaseEntity.succ(fileInfoList);
    }
}
