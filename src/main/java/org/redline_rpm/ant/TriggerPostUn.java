package org.redline_rpm.ant;

import static org.redline_rpm.header.Flags.SCRIPT_TRIGGERPOSTUN;

/**
 * Object describing an RPM TriggerPostUn
 */
public class TriggerPostUn extends AbstractTrigger {

    @Override
    public int getFlag() {
        return SCRIPT_TRIGGERPOSTUN;
    }
}
