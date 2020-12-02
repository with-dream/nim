package com.example.server.user;

import com.example.server.ServerList;
import com.example.server.entity.UserModel;
import com.example.server.entity.UserResultModel;
import com.example.server.service.SysService;
import com.example.server.service.UserService;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping("/user")
public class UserController {
    Gson gson = new Gson();

    @Autowired
    UserService userService;

    @RequestMapping(value = "/login")
    public String login(@RequestParam(value = "name") String name, @RequestParam(value = "pwd") String pwd) {
        UserModel userModel = new UserModel();
        userModel.name = name;
        userModel.pwd = pwd;
        UserResultModel result = userService.login(userModel);
        if (result == null) {
            result = new UserResultModel();
            result.code = -1;
        } else {
            result.imUrl = ServerList.SERVER_LIST;
        }

        result.code = 0;
        String res = gson.toJson(result);
        System.out.println("==>" + res);

        return res;
    }

    @RequestMapping(value = "/unlogin")
    public String unlogin(@RequestParam(value = "uuid") String uuid) {

        return "";
    }

    @RequestMapping(value = "/regist")
    @ResponseBody
    public String regist(@RequestParam(value = "name") String name, @RequestParam(value = "pwd") String pwd) {
        UserModel userModel = new UserModel();
        userModel.name = name;
        userModel.pwd = pwd;
        userModel.uuid = UuidManager.getInstance().getUuid();
        userModel.registTime = new java.sql.Date(new Date().getTime());
        int res = userService.regist(userModel);
        System.err.println("regist res==>" + res);
        return "==>succ";
    }
}
