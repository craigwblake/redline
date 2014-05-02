package org.redline_rpm;

import org.redline_rpm.header.Format;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.nio.channels.Channels;

import static org.junit.Assert.assertEquals;

public class ScannerTest extends TestBase
{

    @Test
    public void scanNoArchRPMTest() throws Exception {
        Scanner.main ( new String[]{ getTestResourcesDirectory ( ) + File.separator + "rpm-1-1.0-1.noarch.rpm" } );
    }

    @Test
    public void scanSomeArchTest() throws Exception {
        Scanner.main ( new String[]{ getTestResourcesDirectory ( ) + File.separator + "rpm-3-1.0-1.somearch.rpm" } );
    }

   @Test
   public void scanXZArchTest() throws Exception {
       Scanner.main ( new String[]{ getTestResourcesDirectory ( ) + File.separator + "rpmtest-3-0.1.XZ.fc19.noarch.rpm" } );
   }

    @Test
    public void setHeaderStartAndEndPosition() throws Exception {
        Format format = new Scanner().run(channelWrapper(getTestResourcesDirectory ( ) + File.separator + "rpm-1-1.0-1.noarch.rpm"));
        assertEquals(280, format.getHeader().getStartPos());
        assertEquals(4760, format.getHeader().getEndPos());
    }

    private ReadableChannelWrapper channelWrapper(String filename) throws Exception {
        return new ReadableChannelWrapper( Channels.newChannel(new FileInputStream(filename)));
    }
}
