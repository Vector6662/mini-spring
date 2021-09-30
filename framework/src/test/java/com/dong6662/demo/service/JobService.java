package com.dong6662.demo.service;

import com.dong6662.minispring.bean.AutoWired;
import com.dong6662.minispring.bean.Component;

@Component
public class JobService {
    @AutoWired
    UserService userService;
//    下面注入的方式是错误的，原因见log的2021/7/10
//    @AutoWired
//    Rapper rapper;
    public String searchJob(){
        return userService.searchUser()+" search job!";
    }
}
