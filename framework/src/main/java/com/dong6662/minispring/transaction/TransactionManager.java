package com.dong6662.minispring.transaction;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class TransactionManager {
    private final Logger logger = LoggerFactory.getLogger(TransactionManager.class);

    //对有@Transactional的类进行代理，后续要对有@Transactional注解的方法进行代理
    public Object transactionProxy(Class<?> clazz){
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> {
            try {
                beginTransaction();
                Object o = proxy.invokeSuper(obj, args);
                commit();
                return o;
            } catch (Throwable throwable) {
                rollback();
                throw new Throwable(throwable);
            }finally {
                release();
            }
        });
        return enhancer.create();
    }

    private void beginTransaction(){
        try {
            ConnectionUtils.threadConnection().setAutoCommit(false);
            logger.info("开始事务...");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void commit(){
        try {
            ConnectionUtils.threadConnection().commit();
            logger.info("事务提交");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void rollback(){
        try {
            ConnectionUtils.threadConnection().rollback();
            logger.warn("事务回滚");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void release(){
        try {
            ConnectionUtils.threadConnection().close();
            ConnectionUtils.removeConnection();
            logger.info("释放连接");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

}
