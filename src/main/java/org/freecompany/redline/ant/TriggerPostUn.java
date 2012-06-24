package org.freecompany.redline.ant;

import static org.freecompany.redline.header.Flags.SCRIPT_TRIGGERPOSTUN;

/**
 * Object describing an RPM TriggerPostUn
 */
public class TriggerPostUn extends AbstractTrigger {

    @Override
    public int getFlag() {
        return SCRIPT_TRIGGERPOSTUN;
    }
}
