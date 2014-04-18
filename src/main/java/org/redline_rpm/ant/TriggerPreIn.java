package org.redline_rpm.ant;

import static org.redline_rpm.header.Flags.SCRIPT_TRIGGERPREIN;

/**
 * Object describing an RPM TriggerPreIn
 */
public class TriggerPreIn extends AbstractTrigger {

    @Override
    public int getFlag() {
        return SCRIPT_TRIGGERPREIN;
    }
}
