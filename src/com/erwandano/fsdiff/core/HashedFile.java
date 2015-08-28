package com.erwandano.fsdiff.core;

import java.nio.file.Path;

/**
 * Stores the hashes of a file
 */
public class HashedFile {

    /**
     * Path pointing to the hashed file
     */
    private Path path;
    /**
     * MD5 hash
     */
    private String md5;
    /**
     * SHA1 hash --unused
     */
    private String sha1;
    /**
     * SHA256 hash --unused
     */
    private String sha256;

    public HashedFile(Path filePath) {
        path = filePath;
        md5 = null;
        sha1 = null;
        sha256 = null;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getSha1() {
        return sha1;
    }

    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }

    public String getSha256() {
        return sha256;
    }

    public void setSha256(String sha256) {
        this.sha256 = sha256;
    }

    public boolean isEqual(HashedFile hashedFile) {
        boolean equal = true;
        if(md5!=null && this.md5.compareTo(hashedFile.getMd5()) != 0)
            equal = false;
        if(equal && sha1!=null && this.sha1.compareTo(hashedFile.getSha1()) != 0)
            equal = false;
        if(equal && sha256!=null && this.sha256.compareTo(hashedFile.getSha256()) != 0)
            equal = false;
        return equal;
    }

}
