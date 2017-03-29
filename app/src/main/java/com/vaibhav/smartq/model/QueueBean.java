package com.vaibhav.smartq.model;

/**
 * Created by vaibhav on 9/15/2016.
 */
public class QueueBean {
    private String name;
    private String description;
    private Integer max;
    private Integer current;

    public QueueBean(){

    }

    public QueueBean(String name, String description, Integer max, Integer current){

        setName(name);
        setDescription(description);
        setMax(max);
        setCurrent(current);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.toUpperCase();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getMax() {
        return max;
    }

    public void setMax(Integer max) {
        this.max = max;
    }

    public Integer getCurrent() {
        return current;
    }

    public void setCurrent(Integer current) {
        this.current = current;
    }

}
