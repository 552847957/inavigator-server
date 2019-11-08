package ru.sberbank.syncserver2.service.generator.single.data;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Created by IntelliJ IDEA.
 * User: sbt-kozhinskiy-lb
 * Date: 27.01.12
 * Time: 19:32
 * To change this template use File | Settings | File Templates.
 */
public class ETLDatabase {
    private String name;
    private String provider;
    private String url;
    private String user;
    private String password;
    private String jndiName;
    private boolean disabled;

    @XmlAttribute(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlAttribute(name = "provider")
    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    @XmlAttribute(name = "url")
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @XmlAttribute(name = "user")
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @XmlAttribute(name = "password")
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @XmlAttribute(name = "jndiName")
    public String getJndiName() {
        return jndiName;
    }

    public void setJndiName(String jndiName) {
        this.jndiName = jndiName;
    }

    @XmlAttribute(name = "disabled")
    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ETLDatabase that = (ETLDatabase) o;

        if (disabled != that.disabled) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (password != null ? !password.equals(that.password) : that.password != null) return false;
        if (provider != null ? !provider.equals(that.provider) : that.provider != null) return false;
        if (url != null ? !url.equals(that.url) : that.url != null) return false;
        if (user != null ? !user.equals(that.user) : that.user != null) return false;
        if (jndiName != null ? !jndiName.equals(that.jndiName) : that.jndiName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (provider != null ? provider.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (user != null ? user.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (jndiName != null ? jndiName.hashCode() : 0);
        result = 31 * result + (disabled ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ETLDatabase{" +
                "name='" + name + '\'' +
                ", provider='" + provider + '\'' +
                ", url='" + url + '\'' +
                ", user='" + user + '\'' +
                ", password='" + password + '\'' +
                ", jndiName='" + jndiName + '\'' +
                '}';
    }
}
