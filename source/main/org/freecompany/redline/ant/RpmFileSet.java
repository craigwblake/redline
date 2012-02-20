package org.freecompany.redline.ant;

import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.TarFileSet;
import org.freecompany.redline.payload.Directive;

/**
 * A {@code RpmFileSet} is a {@link FileSet} to support RPM directives that can't be expressed
 * using ant's built-in {@code FileSet} classes.
 */
public class RpmFileSet extends TarFileSet {

    /**
     * A bit num representing the RPM file attributes.
     *
     * @see org.freecompany.redline.payload.Directive
     */
    private Directive directive = new Directive();

    /**
     * Constructor for {@code RpmFileSet}
     */
    public RpmFileSet() {
        super();
    }

    /**
     * Constructor using a fileset arguement.
     *
     * @param fileset the {@link FileSet} to use
     */
    protected RpmFileSet(FileSet fileset) {
        super(fileset);
    }

    /**
     * Constructor using a archive fileset argument.
     *
     * @param fileset the {@code RpmFileSet} to use
     */
    protected RpmFileSet(RpmFileSet fileset) {
        super(fileset);
        directive = fileset.directive;
    }

    public Directive getDirective() {
        return directive;
    }

    /**
     * Supports RPM's {@code %config} directive, used to flag the specified file as being a configuration file.
     * RPM performs additional processing for config files when packages are erased, and during installations
     * and upgrades.
     * <p/>
     * Permitted values for this directive are:
     * <ul>
     * <li> {@code true}    (equivalent to specifying {@code %config}
     * <li> {@code false}     (equivalent to omitting {@code %config})
     * </ul>
     *
     * @see <a href="http://www.rpm.org/max-rpm-snapshot/s1-rpm-inside-files-list-directives.html">rpm.com</a>
     * @see #directive
     *
     * @param config to set
     */
    public void setConfig(boolean config) {
        checkRpmFileSetAttributesAllowed();
        if(config) {
            directive.set(Directive.RPMFILE_CONFIG);
        } else {
            directive.unset(Directive.RPMFILE_CONFIG);
        }
    }

    /**
     * Supports RPM's {@code %config(noreplace)} directive. This directive modifies how RPM manages edited config
     * files.
     * <p/>
     * Permitted values for this directive are:
     * <ul>
     * <li> {@code true}    (equivalent to specifying {@code %noreplace}
     * <li> {@code false}     (equivalent to omitting {@code %noreplace})
     * </ul>
     *
     * @see <a href="http://www-uxsup.csx.cam.ac.uk/~jw35/docs/rpm_config.html">{@code noreplace} details</a>
     * @see #directive
     *
     * @param noReplace to set
     */
    public void setNoReplace(boolean noReplace) {
        checkRpmFileSetAttributesAllowed();
        if(noReplace) {
            directive.set(Directive.RPMFILE_NOREPLACE);
        } else {
            directive.unset(Directive.RPMFILE_NOREPLACE);
        }
    }

    /**
     * Supports RPM's {@code %doc} directive, which flags the files as being documentation.  RPM keeps track of
     * documentation files in its database, so that a user can easily find information about an installed package.
     * <p/>
     * Permitted values for this directive are:
     * <ul>
     * <li> {@code true}    (equivalent to specifying {@code %doc}
     * <li> {@code false}     (equivalent to omitting {@code %doc})
     * </ul>
     *
     * @see <a href="http://www.rpm.org/max-rpm-snapshot/s1-rpm-inside-files-list-directives.html">rpm.com</a>
     * @see #directive
     *
     * @param doc to set
     */
    public void setDoc(boolean doc) {
        checkRpmFileSetAttributesAllowed();
        if(doc) {
            directive.set(Directive.RPMFILE_DOC);
        } else {
            directive.unset(Directive.RPMFILE_DOC);
        }
    }

    /**
     * Return a ArchiveFileSet that has the same properties
     * as this one.
     *
     * @return the cloned archiveFileSet
     */
    public Object clone() {
        if (isReference()) {
            return getRef(getProject()).clone();
        }
        return super.clone();
    }

    /**
     * A check attributes for TarFileSet.
     * If there is a reference, and
     * it is a TarFileSet, the tar fileset attributes
     * cannot be used.
     */
    private void checkRpmFileSetAttributesAllowed() {
        if (getProject() == null
                || (isReference()
                && (getRefid().getReferencedObject(
                getProject())
                instanceof RpmFileSet))) {
            checkAttributesAllowed();
        }
    }
}
