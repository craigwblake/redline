package org.freecompany.redline.ant;

import static org.freecompany.redline.header.Flags.SCRIPT_TRIGGERUN;

/**
 * Object describing an RPM TriggerUn
 */
public class TriggerUn extends AbstractTrigger {

    @Override
    public int getFlag() {
        return SCRIPT_TRIGGERUN;
    }
}
