package org.redline_rpm.ant;

import static org.redline_rpm.header.Flags.SCRIPT_TRIGGERUN;

/**
 * Object describing an RPM TriggerUn
 */
public class TriggerUn extends AbstractTrigger {

    @Override
    public int getFlag() {
        return SCRIPT_TRIGGERUN;
    }
}
