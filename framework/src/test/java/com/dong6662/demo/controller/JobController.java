package com.dong6662.demo.controller;

import com.dong6662.demo.service.JobService;
import com.dong6662.demo.service.Rap;
import com.dong6662.minispring.bean.AutoWired;
import com.dong6662.minispring.web.mvc.Controller;
import com.dong6662.minispring.web.mvc.RequestMapping;

@Controller
public class JobController {
    @AutoWired
    JobService jobService;

    @AutoWired
    Rap rap;

    @RequestMapping(value = "/")
    public String hello(){
        return "hello";
    }

    @RequestMapping(value = "/job")
    public String getJob(){
        return jobService.searchJob();
    }

    @RequestMapping(value = "/doRap")
    public String doRap(){
        return rap.rap();
    }
}
