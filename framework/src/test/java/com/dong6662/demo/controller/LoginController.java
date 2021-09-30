package com.dong6662.demo.controller;

import com.dong6662.demo.service.JobService;
import com.dong6662.demo.service.Rap;
import com.dong6662.demo.service.Rapper;
import com.dong6662.demo.service.UserService;
import com.dong6662.minispring.bean.AutoWired;
import com.dong6662.minispring.bean.Component;
import com.dong6662.minispring.web.mvc.Controller;
import com.dong6662.minispring.web.mvc.RequestMapping;

@Controller
public class LoginController {
    @AutoWired
    UserService userService;



//    @AutoWired
//    Rap rap;

    @RequestMapping(value = "/user")
    public String getUser(){
        return userService.searchUser();
    }

}
