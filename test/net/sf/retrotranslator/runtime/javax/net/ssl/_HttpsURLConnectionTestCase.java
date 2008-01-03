/***
 * Retrotranslator: a Java bytecode transformer that translates Java classes
 * compiled with JDK 5.0 into classes that can be run on JVM 1.4.
 *
 * Copyright (c) 2005 - 2008 Taras Puchko
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.sf.retrotranslator.runtime.javax.net.ssl;

import junit.framework.*;
import javax.net.ssl.*;
import javax.security.auth.x500.X500Principal;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.security.*;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.math.BigInteger;

/**
 * @author Taras Puchko
 */
public class _HttpsURLConnectionTestCase extends TestCase {

    public void testGetLocalPrincipal() throws Exception {
        final X500Principal x500Principal = new X500Principal("");
        HttpsURLConnection httpsURLConnection = new MockHttpsURLConnection() {
            public Certificate[] getLocalCertificates() {
                return new Certificate[] {new MockX509Certificate(x500Principal)};
            }
        };
        assertSame(x500Principal, httpsURLConnection.getLocalPrincipal());
    }

    public void testGetLocalPrincipal_Empty() throws Exception {
        HttpsURLConnection httpsURLConnection = new MockHttpsURLConnection();
        assertNull(httpsURLConnection.getLocalPrincipal());
    }

    public void testGetPeerPrincipal() throws Exception {
        final X500Principal x500Principal = new X500Principal("");
        HttpsURLConnection httpsURLConnection = new MockHttpsURLConnection() {
            public Certificate[] getServerCertificates() {
                return new Certificate[] {new MockX509Certificate(x500Principal)};
            }
        };
        assertSame(x500Principal, httpsURLConnection.getPeerPrincipal());
    }

    private static class MockHttpsURLConnection extends HttpsURLConnection {

        public MockHttpsURLConnection() throws MalformedURLException {
            super(new URL("https://localhost"));
        }

        public String getCipherSuite() {
            return null;
        }

        public Certificate[] getLocalCertificates() {
            return null;
        }

        public Certificate[] getServerCertificates() throws SSLPeerUnverifiedException {
            throw new SSLPeerUnverifiedException("");
        }

        public void disconnect() {
        }

        public boolean usingProxy() {
            return false;
        }

        public void connect() throws IOException {
        }
    }

    private static class MockX509Certificate extends X509Certificate {

        private X500Principal x500Principal;

        public MockX509Certificate(X500Principal x500Principal) {
            this.x500Principal = x500Principal;
        }

        public X500Principal getSubjectX500Principal() {
            return x500Principal;
        }

        public void checkValidity() throws CertificateExpiredException, CertificateNotYetValidException {
        }

        public void checkValidity(Date date) throws CertificateExpiredException, CertificateNotYetValidException {
        }

        public int getVersion() {
            return 0;
        }

        public BigInteger getSerialNumber() {
            return null;
        }

        public Principal getIssuerDN() {
            return null;
        }

        public Principal getSubjectDN() {
            return null;
        }

        public Date getNotBefore() {
            return null;
        }

        public Date getNotAfter() {
            return null;
        }

        public byte[] getTBSCertificate() throws CertificateEncodingException {
            return new byte[0];
        }

        public byte[] getSignature() {
            return new byte[0];
        }

        public String getSigAlgName() {
            return null;
        }

        public String getSigAlgOID() {
            return null;
        }

        public byte[] getSigAlgParams() {
            return new byte[0];
        }

        public boolean[] getIssuerUniqueID() {
            return new boolean[0];
        }

        public boolean[] getSubjectUniqueID() {
            return new boolean[0];
        }

        public boolean[] getKeyUsage() {
            return new boolean[0];
        }

        public int getBasicConstraints() {
            return 0;
        }

        public byte[] getEncoded() throws CertificateEncodingException {
            return new byte[0];
        }

        public void verify(PublicKey key) throws CertificateException, NoSuchAlgorithmException,
                InvalidKeyException, NoSuchProviderException, SignatureException {
        }

        public void verify(PublicKey key, String sigProvider) throws CertificateException, NoSuchAlgorithmException,
                InvalidKeyException, NoSuchProviderException, SignatureException {
        }

        public String toString() {
            return null;
        }

        public PublicKey getPublicKey() {
            return null;
        }

        public boolean hasUnsupportedCriticalExtension() {
            return false;
        }

        public Set<String> getCriticalExtensionOIDs() {
            return null;
        }

        public Set<String> getNonCriticalExtensionOIDs() {
            return null;
        }

        public byte[] getExtensionValue(String oid) {
            return new byte[0];
        }
    }

}