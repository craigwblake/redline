package org.freecompany.redline.ant;

import static org.freecompany.redline.header.Flags.SCRIPT_TRIGGERPREIN;

/**
 * Object describing an RPM TriggerPreIn
 */
public class TriggerPreIn extends AbstractTrigger {

    @Override
    public int getFlag() {
        return SCRIPT_TRIGGERPREIN;
    }
}
