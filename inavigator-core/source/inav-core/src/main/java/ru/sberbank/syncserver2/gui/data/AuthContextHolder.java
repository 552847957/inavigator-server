package ru.sberbank.syncserver2.gui.data;

import java.io.Serializable;

/**
 * Proxy for context
 * User: sbt-bubnov-vy
 * Date: 24.07.12
 * Time: 18:41
 */
public class AuthContextHolder implements Serializable{
    AuthContext authContext;

    public AuthContext getAuthContext() {
        return authContext;
    }

    public void setAuthContext(AuthContext authContext) {
        this.authContext = authContext;
    }
}
