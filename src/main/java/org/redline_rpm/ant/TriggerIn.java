package org.redline_rpm.ant;

import static org.redline_rpm.header.Flags.SCRIPT_TRIGGERIN;

/**
 * Object describing an RPM TriggerIn
 */
public class TriggerIn extends AbstractTrigger {

    @Override
    public int getFlag() {
        return SCRIPT_TRIGGERIN;
    }
}
