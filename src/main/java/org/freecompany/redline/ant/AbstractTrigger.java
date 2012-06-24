package org.freecompany.redline.ant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

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

    public Map< String, String> getDepends() {
        Map< String, String> dependsMap = new HashMap< String, String>();
        for ( Iterator< Depends> i = this.depends.iterator(); i.hasNext();) {
            Depends d = i.next();
            dependsMap.put( d.getName(), d.getVersion());
        }
        return dependsMap;
    }

    public abstract int getFlag();

}
