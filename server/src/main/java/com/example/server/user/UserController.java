package com.example.server.user;

import com.example.server.ServerList;
import com.example.server.service.UserService;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import user.LoginModel;

@RestController
@RequestMapping("/user")
public class UserController {
    Gson gson = new Gson();

    @Autowired
    UserService userService;

    @RequestMapping(value = "/login")
    public String login(@RequestParam(value = "name") String name, @RequestParam(value = "pwd") String pwd) {
        LoginModel loginModel = new LoginModel();
        loginModel.imUrl = ServerList.SERVER_LIST;
        loginModel.id = Integer.parseInt(pwd);
        loginModel.code = 0;
        return gson.toJson(loginModel);
    }

    @RequestMapping(value = "/unlogin")
    public String unlogin(@RequestParam(value = "uuid") String uuid) {

        return "";
    }

    @RequestMapping(value = "/regist")
    @ResponseBody
    public String regist(@RequestParam(value = "name") String name, @RequestParam(value = "pwd") String pwd) {
//        UserModel userModel = new UserModel();
//        userModel.name = name;
//        userModel.pwd = pwd;
//        userModel.uuid = 111;
//        int res = userService.regist(userModel);
//        System.err.println("regist res==>" + res);
        System.err.println("sel res==>" + userService.sel());
        return "==>succ";
    }
}
