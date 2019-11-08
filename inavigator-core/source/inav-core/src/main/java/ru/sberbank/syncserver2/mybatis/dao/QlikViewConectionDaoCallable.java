package ru.sberbank.syncserver2.mybatis.dao;

public interface QlikViewConectionDaoCallable<V, W> {
    V onCall(W strMsg) throws Exception;
}
