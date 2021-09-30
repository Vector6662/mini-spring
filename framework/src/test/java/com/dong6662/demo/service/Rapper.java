package com.dong6662.demo.service;


import com.dong6662.minispring.bean.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class Rapper implements Rap{
    private Logger log = LoggerFactory.getLogger(Rapper.class);

    @Override
    public String rap() {
        return "I am rapping!";
    }
}
