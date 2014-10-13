package org.redline_rpm;

import org.redline_rpm.header.Format;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.nio.channels.Channels;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

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
    @Test
    public void fileModesHeaderIsCorrect() throws Exception {
        Format format = new Scanner().run(channelWrapper(getTestResourcesDirectory ( ) + File.separator + "rpm-1-1.0-1.noarch.rpm"));
        String rpmDescription = format.toString();
        Matcher matcher = Pattern.compile(".*filemodes\\[[^\\]]*\\]\\n[^0-9-]*([^\\n]*).*", Pattern.DOTALL).matcher(rpmDescription);
        matcher.matches();
        String [] fileModesFromString = matcher.group(1).split(", ");
        //String [] actual = {"-32348", "-24065", "16877", "-32275", "-32275", "-32275", "-32275", "-24083", "-32275", "16877", "-32348", "-32348", "16877", "-32348", "-24065,"};
        String [] expectedFileModes = {"33188", "41471", "16877", "33261", "33261", "33261", "33261", "41453", "33261", "16877", "33188", "33188", "16877", "33188", "41471"};
        assertArrayEquals(expectedFileModes, fileModesFromString);
    }

    private ReadableChannelWrapper channelWrapper(String filename) throws Exception {
        return new ReadableChannelWrapper( Channels.newChannel(new FileInputStream(filename)));
    }
}
