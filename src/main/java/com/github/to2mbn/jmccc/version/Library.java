package com.github.to2mbn.jmccc.version;

import java.io.File;
import java.util.Objects;
import com.github.to2mbn.jmccc.option.MinecraftDirectory;

public class Library {

    private String domain;
    private String name;
    private String version;
    private String customUrl;

    /**
     * Creates a library.
     * 
     * @param domain the domain of the library
     * @param name the name of the library
     * @param version the version of the library
     * @throws NullPointerException if <code>domain==null||name==null||version==null</code>
     */
    public Library(String domain, String name, String version) {
        this(domain, name, version, null);
    }

    /**
     * Creates a library with the custom download url.
     * 
     * @param domain the domain of the library
     * @param name the name of the library
     * @param version the version of the library
     * @param customUrl the custom maven repository url
     * @throws NullPointerException if <code>domain==null||name==null||version==null</code>
     */
    public Library(String domain, String name, String version, String customUrl) {
        Objects.requireNonNull(domain);
        Objects.requireNonNull(name);
        Objects.requireNonNull(version);
        this.domain = domain;
        this.name = name;
        this.version = version;
        this.customUrl = customUrl;
    }

    /**
     * Gets the relative path of the library.
     * <p>
     * Use '/' as the separator char, and 'libraries' as the base dir.
     * 
     * @return the relative path of the library
     */
    public String getPath() {
        return domain.replace('.', '/') + "/" + name + "/" + version + "/" + name + "-" + version + ".jar";
    }

    /**
     * Gets the name of the library.
     * 
     * @return the name of the library
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the domain of this library.
     * 
     * @return the domain of this library
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Gets the version of this library.
     * 
     * @return the version of this library
     */
    public String getVersion() {
        return version;
    }

    /**
     * Gets the custom maven repository url, null for default repository.
     * 
     * @return the custom maven repository url, null for default repository
     */
    public String getCustomUrl() {
        return customUrl;
    }

    /**
     * Checks if the library is missing in the given minecraft directory.
     * 
     * @param minecraftDir the minecraft directory to check
     * @return true if the library is missing in the given minecraft directory
     */
    public boolean isMissing(MinecraftDirectory minecraftDir) {
        return !new File(minecraftDir.getLibraries(), getPath()).isFile();
    }

    @Override
    public String toString() {
        return domain + ":" + name + ":" + version;
    }

}