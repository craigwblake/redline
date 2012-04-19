package org.freecompany.redline;

import java.io.File;

import org.testng.annotations.Test;

public class ScannerTest extends TestBase
{

    @Test
    public void scanTest() throws Exception {
        Scanner.main ( new String[]{ getTestResourcesDirectory ( ) + File.separator + "rpm-1-1.0-1.noarch.rpm" } );
    }
}
