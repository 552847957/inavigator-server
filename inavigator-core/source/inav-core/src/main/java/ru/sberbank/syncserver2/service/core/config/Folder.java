package ru.sberbank.syncserver2.service.core.config;

/**
 * @author Sergey Erin
 *
 */
public class Folder {

    private String code;
    private Integer sortOrder;
    private String  description;

    /**
     * @param code
     * @param sortOrder
     */
    public Folder(String code, Integer sortOrder, String description) {
        this.code = code;
        this.sortOrder = sortOrder;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "Folder [code=" + code + ", sortOrder=" + sortOrder;
    }
}
