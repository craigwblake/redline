package org.redline_rpm;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.bc.BcPBESecretKeyDecryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;
import org.redline_rpm.header.Signature;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.logging.Logger;

import static java.util.logging.Logger.getLogger;
import static org.redline_rpm.ChannelWrapper.Key;
import static org.redline_rpm.header.AbstractHeader.Entry;
import static org.redline_rpm.header.Signature.SignatureTag.LEGACY_PGP;
import static org.redline_rpm.header.Signature.SignatureTag.RSAHEADER;

/**
 * Generates PGP signatures for NIO channels
 */
public class SignatureGenerator {

    protected static final int SIGNATURE_SIZE = 287;
    protected final boolean enabled;
    protected Entry< byte[]> headerOnlyRSAEntry;
    protected Entry< byte[]> headerAndPayloadPGPEntry;
    protected final PGPPrivateKey privateKey;
    protected Key< byte[]> headerOnlyKey = null;
    protected Key< byte[]> headerAndPayloadKey = null;
    private Logger logger = getLogger( SignatureGenerator.class.getName());

    public SignatureGenerator( PGPPrivateKey privateKey ) {
        this.privateKey = privateKey;
        this.enabled = privateKey != null;
    }

    public SignatureGenerator( File privateKeyRingFile, String privateKeyId, String privateKeyPassphrase ) {
        if ( privateKeyRingFile != null ) {
            PGPSecretKeyRingCollection keyRings = readKeyRings( privateKeyRingFile );
            PGPSecretKey secretKey = findMatchingSecretKey( keyRings, privateKeyId );
            PGPPrivateKey key = null;
            try { key = extractPrivateKey( secretKey, privateKeyPassphrase ); }catch( IllegalArgumentException e){
                logger.warning("Private Key could not be extracted and therefore a signature will not be generated! "+e.getLocalizedMessage());
            }
            privateKey=key; 
            this.enabled = key!=null?true:false;
        } else {
            privateKey = null;
            this.enabled = false;
        }
    }

    @SuppressWarnings("unchecked")
	public void prepare( Signature signature ) {
        if ( enabled ) {
            headerOnlyRSAEntry = ( Entry< byte[]> ) signature.addEntry( RSAHEADER, SIGNATURE_SIZE );
            headerAndPayloadPGPEntry = ( Entry< byte[]> ) signature.addEntry( LEGACY_PGP, SIGNATURE_SIZE );
        }
    }


    public void startBeforeHeader( WritableChannelWrapper output ) {
        if ( enabled ) {
            headerOnlyKey = output.start( privateKey, getAlgorithm());
            headerAndPayloadKey = output.start( privateKey, getAlgorithm());
        }
    }


    public void finishAfterHeader( WritableChannelWrapper output ) {
        finishEntry( output, headerOnlyRSAEntry, headerOnlyKey );
    }

    public void finishAfterPayload( WritableChannelWrapper output ) {
        finishEntry( output, headerAndPayloadPGPEntry, headerAndPayloadKey );
    }

    protected PGPSecretKeyRingCollection readKeyRings( File privateKeyRingFile ) {
        try {
            InputStream keyInputStream = new BufferedInputStream( new FileInputStream( privateKeyRingFile ) );
            InputStream decoderStream = PGPUtil.getDecoderStream( keyInputStream );
            try {
                return new PGPSecretKeyRingCollection( decoderStream , new BcKeyFingerprintCalculator());

            } finally {
                decoderStream.close();
            }
        } catch ( IOException e ) {
            throw new IllegalArgumentException( "Could not read key ring file: " + privateKeyRingFile, e );
        } catch ( PGPException e ) {
            throw new IllegalArgumentException( "Could not extract key rings from: " + privateKeyRingFile, e );
        }
    }

    protected PGPSecretKey findMatchingSecretKey( PGPSecretKeyRingCollection keyRings, String privateKeyId ) {
        privateKeyId = privateKeyId != null ? privateKeyId.toLowerCase() : null;

        @SuppressWarnings( "unchecked" )
        Iterator< PGPSecretKeyRing> iter = keyRings.getKeyRings();
        while ( iter.hasNext() ) {
            PGPSecretKeyRing keyRing = iter.next();

            @SuppressWarnings( "unchecked" )
            Iterator< PGPSecretKey> keyIter = keyRing.getSecretKeys();
            while ( keyIter.hasNext() ) {
                PGPSecretKey key = keyIter.next();
                if ( key.isSigningKey() && isMatchingKeyId( key, privateKeyId ) ) {
                    return key;
                }
            }
        }

        throw new IllegalArgumentException( "Can't find signing key in key rings." );
    }

    protected boolean isMatchingKeyId( PGPSecretKey key, String privateKeyId ) {
        if (privateKeyId == null) {
            return true;
        }

        return Long.toHexString( key.getKeyID() ).endsWith( privateKeyId );
    }

    protected PGPPrivateKey extractPrivateKey( PGPSecretKey secretKey, String privateKeyPassphrase ) {
        BcPBESecretKeyDecryptorBuilder secretKeyDecryptorBuilder = new BcPBESecretKeyDecryptorBuilder( new BcPGPDigestCalculatorProvider() );
        try {
            PBESecretKeyDecryptor secretKeyDecryptor = secretKeyDecryptorBuilder.build( privateKeyPassphrase.toCharArray() );
            return secretKey.extractPrivateKey( secretKeyDecryptor );
        } catch ( Exception e ) {
            throw new IllegalArgumentException( "Could not extract private key from key ring", e );
        }
    }

    protected void finishEntry( WritableChannelWrapper output, Entry< byte[]> entry, Key< byte[]> key ) {
        if ( enabled ) {
            checkKey( key );
            checkEntry( entry );
            entry.setValues( output.finish( key ) );
        }
    }

    protected void checkEntry( Entry< byte[]> entry ) {
        if ( entry == null ) {
            throw new IllegalStateException( "Entry not initialized. Please call prepare() first" );
        }
    }

    protected void checkKey( Key< byte[]> key ) {
        if ( key == null ) {
            throw new IllegalStateException( "Key is not initialized. Please call startBeforeHeader() first." );
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    protected int getAlgorithm() {
        return privateKey != null ? privateKey.getPublicKeyPacket().getAlgorithm() : 0;
    }
}
