package com.workable.errorhandlersample.models;


import com.google.gson.annotations.SerializedName;

public class GithubUser {

    @SerializedName("login")
    private String login;

    @SerializedName("id")
    private String id;

    @SerializedName("avatar_url")
    private String avatar_url;

    @SerializedName("url")
    private String url;

    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("location")
    private String location;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAvatar_url() {
        return avatar_url;
    }

    public void setAvatar_url(String avatar_url) {
        this.avatar_url = avatar_url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
