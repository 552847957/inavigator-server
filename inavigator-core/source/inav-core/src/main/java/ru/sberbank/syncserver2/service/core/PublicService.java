package ru.sberbank.syncserver2.service.core;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by sbt-shakhov-in on 26.02.16.
 */
public interface PublicService {
    void request(HttpServletRequest request,HttpServletResponse response);
}
