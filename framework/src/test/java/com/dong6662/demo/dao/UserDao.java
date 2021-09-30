package com.dong6662.demo.dao;

import com.dong6662.minispring.bean.Component;
import com.dong6662.minispring.transaction.TransactionManager;
import com.dong6662.minispring.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Transactional
public class UserDao {
    private static final Logger logger = LoggerFactory.getLogger(UserDao.class);
    public String searchUser(){
        logger.info("事务内容");
        return "gie gie~";
    }

}
