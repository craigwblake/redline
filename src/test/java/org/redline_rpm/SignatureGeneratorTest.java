package org.redline_rpm;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;

import static org.junit.Assert.assertTrue;

public class SignatureGeneratorTest extends TestBase {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testReadingFirstKey() throws Exception {
        SignatureGenerator generator = new SignatureGenerator( new File( getFileResource( "/pgp/secring.gpg" ) ), null, "redline" );
        assertTrue( generator.isEnabled() );
    }

    @Test
    public void testFindByKey() throws Exception {
        SignatureGenerator generator = new SignatureGenerator( new File( getFileResource( "/pgp/secring.gpg" )), "5A186608", "redline" );
        assertTrue( generator.isEnabled() );
    }

    @Test

    public void testThrowsExceptionWhenDsaKeyUsed() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Only RSA keys are supported");

        new SignatureGenerator(new File(getFileResource("/pgp-dsa/secring.gpg")), "89DD9A34", "redline");
    }
}
