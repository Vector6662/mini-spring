package com.dong6662.demo.service;

import com.dong6662.demo.dao.UserDao;
import com.dong6662.minispring.bean.AutoWired;
import com.dong6662.minispring.bean.Component;

@Component
public class UserService {
    @AutoWired
    JobService jobService;
    @AutoWired
    UserDao userDao;

    public String searchUser(){
        return userDao.searchUser();
    }
}
