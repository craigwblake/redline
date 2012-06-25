package org.freecompany.redline.ant;

import static org.freecompany.redline.header.Flags.SCRIPT_TRIGGERIN;

/**
 * Object describing an RPM TriggerIn
 */
public class TriggerIn extends AbstractTrigger {

    @Override
    public int getFlag() {
        return SCRIPT_TRIGGERIN;
    }
}
