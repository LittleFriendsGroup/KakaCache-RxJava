package com.im4j.kakacache.rxjava.demo;

import com.google.gson.annotations.SerializedName;

/**
 * @version alafighting 2016-07
 */
public class GithubRepoEntity {

    private String id;
    private String name;
    @SerializedName("full_name")
    private String fullName;
    private String description;

    public GithubRepoEntity() {
    }
    public GithubRepoEntity(String id, String name, String fullName, String description) {
        this.id = id;
        this.name = name;
        this.fullName = fullName;
        this.description = description;
    }

    @Override
    public String toString() {
        return "GithubRepoEntity{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", fullName='" + fullName + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
