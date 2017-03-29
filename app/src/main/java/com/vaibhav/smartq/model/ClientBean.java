package com.vaibhav.smartq.model;

/**
 * Created by vaibhav on 10/26/2016.
 */

public class ClientBean {

    private String client_name;
    private String client_email;

    public ClientBean(){

    }

    public ClientBean(String client_name, String client_email){
        this.client_name = client_name;
        this.client_email = client_email;
    }

    public String getClient_name() {
        return client_name;
    }

    public void setClient_name(String client_name) {
        this.client_name = client_name;
    }

    public String getClient_email() {
        return client_email;
    }

    public void setClient_email(String client_email) {
        this.client_email = client_email;
    }
}
