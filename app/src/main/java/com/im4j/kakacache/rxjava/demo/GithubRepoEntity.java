package com.im4j.kakacache.rxjava.demo;

import com.im4j.kakacache.rxjava.common.utils.MemorySizeOf;

/**
 * @version alafighting 2016-07
 */
public class GithubRepoEntity implements MemorySizeOf.SizeOf {

    private String id;
    private String name;
    private String description;

    public GithubRepoEntity() {
    }
    public GithubRepoEntity(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    @Override
    public String toString() {
        return "GithubRepoEntity{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public long sizeOf() {
        return MemorySizeOf.sizeOf(id)
                + MemorySizeOf.sizeOf(name)
                + MemorySizeOf.sizeOf(description);
    }
}
