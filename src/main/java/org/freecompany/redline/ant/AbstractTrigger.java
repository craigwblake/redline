package org.freecompany.redline.ant;

import org.freecompany.redline.IntString;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Object describing an RPM Trigger
 */
public abstract class AbstractTrigger {

    protected File script;
    protected List< Depends> depends = new ArrayList< Depends>();

    public void setScript( File script) {
        this.script = script;
    }

    public File getScript() {
        return script;
    }

    public void addDepends( Depends depends) {
        this.depends.add( depends);
    }

    public Map< String, IntString> getDepends() {
        Map< String, IntString> dependsMap = new HashMap< String, IntString>();
        for (Depends d : this.depends) {
            dependsMap.put(d.getName(), new IntString(d.getComparison(), d.getVersion()));
        }
        return dependsMap;
    }

    public abstract int getFlag();

}
