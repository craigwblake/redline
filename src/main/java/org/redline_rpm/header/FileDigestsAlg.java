package org.redline_rpm.header;

public enum FileDigestsAlg {

    MD5(1, "MD5"),
    SHA1(2, "SHA-1"),
    SHA256(8, "SHA-256"),
    SHA384(9, "SHA-384"),
    SHA512(10, "SHA-512");

    private final int code;
    private final String algName;
    FileDigestsAlg(int code, String algName){
        this.code = code;
        this.algName = algName;
    }

    public int getCode() {
        return code;
    }

    public String getAlgName() {
        return algName;
    }
}
