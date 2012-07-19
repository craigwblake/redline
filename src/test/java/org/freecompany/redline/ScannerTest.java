package org.freecompany.redline;

import java.io.File;
import org.junit.Test;

public class ScannerTest extends TestBase
{

    @Test
    public void scanNoArchRPMTest() throws Exception {
        Scanner.main ( new String[]{ getTestResourcesDirectory ( ) + File.separator + "rpm-1-1.0-1.noarch.rpm" } );
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void scanSomeArchTest() throws Exception {
        Scanner.main ( new String[]{ getTestResourcesDirectory ( ) + File.separator + "rpm-3-1.0-1.somearch.rpm" } );
    }
}
