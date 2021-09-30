package com.dong6662.minispring.transaction;

import cn.hutool.core.util.ObjectUtil;
import org.apache.commons.dbcp2.BasicDataSource;


import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionUtils {
    private static final ThreadLocal<Connection> tl = new ThreadLocal<>();
    private static final BasicDataSource basicDataSource = new BasicDataSource();

    static {
        basicDataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        basicDataSource.setUrl("jdbc:mysql://localhost:3306/world");
        basicDataSource.setUsername("root");
        basicDataSource.setPassword("root");
    }

    public static Connection threadConnection(){
        if (ObjectUtil.isNull(tl.get())){
            try {
                tl.set(basicDataSource.getConnection());
                return tl.get();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return tl.get();
    }

    public static void removeConnection(){
        tl.remove();
    }

}
