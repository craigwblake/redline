package org.redline_rpm;

import org.junit.Test;

import java.io.File;
import static org.junit.Assert.assertTrue;

public class SignatureGeneratorTest extends TestBase {

    @Test
    public void testReadingFirstKey() throws Exception {
        SignatureGenerator generator = new SignatureGenerator( new File( getFileResource( "/pgp/secring.gpg" ) ), null, "redline", false );
        assertTrue( generator.isEnabled() );
    }

    @Test
    public void testFindByKey() throws Exception {
        SignatureGenerator generator = new SignatureGenerator( new File( getFileResource( "/pgp/secring.gpg" )), "5A186608", "redline", false );
        assertTrue( generator.isEnabled() );
    }

    @Test
    public void testV3signature() throws Exception {
        SignatureGenerator generator = new SignatureGenerator( new File( getFileResource( "/pgp/secring.gpg" )), "5A186608", "redline", true );
        assertTrue( generator.isEnabled() );
    }
        
}
