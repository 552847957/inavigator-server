package ru.sberbank.syncserver2.service.core.config;

/**
 * @author Sergey Erin
 *
 */
public class BeanProperty {

    private Long id;
    private String code;
    private String value;
    private String description;

    /**
     * @param id
     * @param code
     * @param value
     */
    public BeanProperty(Long id, String code, String value, String description) {
        this.id = id;
        this.code = code;
        this.value = value;
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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return code+"="+value;
    }

}
