package ru.sberbank.syncserver2.service.core.config;

import java.util.List;

/**
 * @author Sergey Erin
 *
 */
public class Bean {

    private Long id;
    private String code;
    private String clazz;
    private String parentCode;
    private String parentProperty;
    private Integer startOrder;
    private String publicServletPath;
    private List<BeanProperty> beanProperties;
    private boolean stopped = false;
    private String description;

    public Bean() {
    }

    /**
     * @param id
     * @param code
     * @param clazz
     * @param parentCode
     * @param parentProperty
     * @param startOrder
     */
    public Bean(Long id, String code, String clazz, String parentCode,
            String parentProperty, Integer startOrder, String publicServletPath, String description) {
        this.id = id;
        this.code = code;
        this.clazz = clazz;
        this.parentCode = parentCode;
        this.parentProperty = parentProperty;
        this.startOrder = startOrder;
        this.publicServletPath = publicServletPath;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public String getParentCode() {
        return parentCode;
    }

    public void setParentCode(String parentCode) {
        this.parentCode = parentCode;
    }

    public String getParentProperty() {
        return parentProperty;
    }

    public void setParentProperty(String parentProperty) {
        this.parentProperty = parentProperty;
    }

    public Integer getStartOrder() {
        return startOrder;
    }

    public void setStartOrder(Integer startOrder) {
        this.startOrder = startOrder;
    }

    public String getPublicServletPath() {
        return publicServletPath;
    }

    public void setPublicServletPath(String publicServletPath) {
        this.publicServletPath = publicServletPath;
    }

    public boolean isPublic(){
        return publicServletPath!=null && publicServletPath.trim().length()>0;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<BeanProperty> getBeanProperties() {
        return beanProperties;
    }

    public void setBeanProperties(List<BeanProperty> beanProperties) {
        this.beanProperties = beanProperties;
    }

    public String getPropertyValue(String propertyName){
        if(beanProperties==null || propertyName==null){
            return null;
        }
        for (int i = 0; i < beanProperties.size(); i++) {
            BeanProperty beanProperty =  beanProperties.get(i);
            String beanPropertyCode = beanProperty.getCode();
            if(propertyName.equalsIgnoreCase(beanPropertyCode)){
                return beanProperty.getValue();
            }
        }
        return null;
    }

    public int getPropertyValueAsInteger(String propertyName, int defaultValue){
        String value = getPropertyValue(propertyName);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    @Override
    public String toString() {
        return "Bean [id=" + id + ", code=" + code + ", clazz=" + clazz
                + ", parentCode=" + parentCode + ", parentProperty="
                + parentProperty + ", startOrder=" + startOrder
                + ", isStopped=" + stopped
                + ", beanProperties=" + beanProperties;
    }

	public boolean isStopped() {
		return stopped;
	}

	public void setStopped(boolean stopped) {
		this.stopped = stopped;
	}

}
