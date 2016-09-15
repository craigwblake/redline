package org.redline_rpm;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class represents a RPM dependency.
 *
 * There are currently four different types known by RPM:
 * - requires
 * - provides
 * - conflicts
 * - obsoletes
 *
 * These can be represented by this class.
 *
 */
public class Dependency {

    private String name;
    private String version;
    private Integer flags;

    /**
     * Creates a new dependency.
     * @param name       Name (e.g. "httpd")
     * @param version    Version (e.g. "1.0")
     * @param flags      Flags, see org.redline_rpm.header.Flags (e.g. "GREATER | EQUAL")
     */
    public Dependency(String name, String version, Integer flags) {
        this.name = name;
        this.version = version;
        this.flags = flags;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public Integer getFlags() {
        return flags;
    }

    /**
     * Returns an array of String with the name of every dependency from a list of dependencies.
     * @param dependencyList    List of dependencies
     * @return String[] with all names of the dependencies
     */
    public static String[] getArrayOfNames(List<Dependency> dependencyList) {
        List<String> nameList = new LinkedList<String>();

        for (Dependency dependency : dependencyList) {
            nameList.add(dependency.getName());
        }

        return nameList.toArray(new String[nameList.size()]);
    }

    /**
     * Returns an array of String with the version of every dependency from a list of dependencies.
     * @param dependencyList    List of dependencies
     * @return String[] with all versions of the dependencies
     */
    public static String[] getArrayOfVersions(List<Dependency> dependencyList) {
        List<String> versionList = new LinkedList<String>();

        for (Dependency dependency : dependencyList) {
            versionList.add(dependency.getVersion());
        }

        return versionList.toArray(new String[versionList.size()]);
    }

    /**
     * Returns an array of Integer with the flags of every dependency from a list of dependencies.
     * @param dependencyList    List of dependencies
     * @return Integer[] with all flags of the dependencies
     */
    public static Integer[] getArrayOfFlags(List<Dependency> dependencyList) {
        List<Integer> flagsList = new LinkedList<Integer>();

        for (Dependency dependency : dependencyList) {
            flagsList.add(dependency.getFlags());
        }

        return flagsList.toArray(new Integer[flagsList.size()]);
    }
  
    /**
     * Returns an array of String with the name of every dependency from a list of dependencies.
     * @param dependencies    List of dependencies
     * @return String[] with all names of the dependencies
     */
    public static String[] getArrayOfNames(Map< String, Dependency> dependencies) {
        List<String> nameList = new LinkedList<String>();

        for (Dependency dependency : dependencies.values()) {
            nameList.add(dependency.getName());
        }

        return nameList.toArray(new String[nameList.size()]);
    }

    /**
     * Returns an array of String with the version of every dependency from a list of dependencies.
     * @param dependencies    List of dependencies
     * @return String[] with all versions of the dependencies
     */
    public static String[] getArrayOfVersions(Map< String, Dependency> dependencies) {
        List<String> versionList = new LinkedList<String>();

        for (Dependency dependency : dependencies.values()) {
            versionList.add(dependency.getVersion());
        }

        return versionList.toArray(new String[versionList.size()]);
    }

    /**
     * Returns an array of Integer with the flags of every dependency from a list of dependencies.
     * @param dependencies    List of dependencies
     * @return Integer[] with all flags of the dependencies
     */
    public static Integer[] getArrayOfFlags(Map< String, Dependency> dependencies) {
        List<Integer> flagsList = new LinkedList<Integer>();

        for (Dependency dependency : dependencies.values()) {
            flagsList.add(dependency.getFlags());
        }

        return flagsList.toArray(new Integer[flagsList.size()]);
    }
}

    
    

