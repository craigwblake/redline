package org.freecompany.redline;

import java.io.File;
import java.net.URL;

/**
 * @author Karl Heinz Marbaise
 */
public class TestBase {
    /**
     * This method will give you back the filename incl. the absolute path name
     * to the resource. If the resource does not exist it will give you back the
     * resource name incl. the path.
     * 
     * It will give you back an absolute path incl. the name which is in the
     * same directory as the the class you've called it from.
     * 
     * @param name
     * @return
     */
    public String getFileResource(String name) {
        URL url = this.getClass().getResource(name);
        if (url != null) {
            return url.getFile();
        } else {
            // We have a file which does not exists
            // We got the path
            url = this.getClass().getResource(".");
            return url.getFile() + name;
        }
    }

    /**
     * Return the base directory of the project.
     * 
     * @return
     */
    public String getMavenBaseDir() {
        // basedir is defined by Maven
        // but the above will not work under Eclipse.
        // So there I'M using user.dir
        return System.getProperty("basedir",
                System.getProperty("user.dir", "."));
    }

    /**
     * Return the target directory of the current project.
     * 
     * @return
     */
    public String getTargetDir() {
        return getMavenBaseDir() + File.separatorChar + "target";
    }

    /**
     * This will give you back the position of a repository which is stored
     * inside the <b>target</b> directory.
     * 
     * @return The directory where the repository has been stored.
     */
    public String getRepositoryDirectory() {
        return getTargetDir() + File.separatorChar + "repos";
    }

    public String getRepositoryDirectory(String supplemental) {
        return getTargetDir() + File.separatorChar + "repos-" + supplemental;
    }

    /**
     * This will give you back the position of an index directory which is
     * stored inside the <b>target</b> directory.
     * 
     * @return The directory where the index is stored.
     */
    public String getIndexDirectory() {
        return getTargetDir() + File.separatorChar + "index.Test";
    }

    public String getIndexDirectory(String supplemental) {
        return getTargetDir() + File.separatorChar + "index-" + supplemental
                + ".Test";
    }

    public String getSrcDirectory() {
        return getMavenBaseDir() + File.separator + "src";
    }

    public String getTestDirectory() {
        return getSrcDirectory() + File.separator + "test";
    }

    public String getTestResourcesDirectory() {
        return getTestDirectory() + File.separator + "resources";
    }
}
