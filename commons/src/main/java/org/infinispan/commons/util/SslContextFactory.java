package org.infinispan.commons.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.infinispan.commons.logging.Log;
import org.infinispan.commons.logging.LogFactory;

/**
 * SslContextFactory.
 *
 * @author Tristan Tarrant
 * @since 5.3
 */
public class SslContextFactory {
   private static final Log log = LogFactory.getLog(SslContextFactory.class);

   public static SSLContext getContext(String keyStoreFileName, char[] keyStorePassword, String trustStoreFileName, char[] trustStorePassword) {
      try {
         KeyManager[] keyManagers = null;
         if (keyStoreFileName != null) {
            KeyStore ks = KeyStore.getInstance("JKS");
            loadKeyStore(ks, keyStoreFileName, keyStorePassword);
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, keyStorePassword);
            keyManagers = kmf.getKeyManagers();
         }

         TrustManager[] trustManagers = null;
         if (trustStoreFileName != null) {
            KeyStore ks = KeyStore.getInstance("JKS");
            loadKeyStore(ks, trustStoreFileName, trustStorePassword);
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            tmf.init(ks);
            trustManagers = tmf.getTrustManagers();
         }

         SSLContext sslContext = SSLContext.getInstance("TLS");
         sslContext.init(keyManagers, trustManagers, null);
         return sslContext;
      } catch (Exception e) {
         throw log.sslInitializationException(e);
      }
   }

   public static SSLEngine getEngine(SSLContext sslContext, boolean useClientMode, boolean needClientAuth) {
      SSLEngine sslEngine = sslContext.createSSLEngine();
      sslEngine.setUseClientMode(useClientMode);
      sslEngine.setNeedClientAuth(needClientAuth);
      return sslEngine;
   }

   private static void loadKeyStore(KeyStore ks, String keyStoreFileName, char[] keyStorePassword) throws IOException, GeneralSecurityException {
      InputStream is = new BufferedInputStream(new FileInputStream(keyStoreFileName));
      try {
         ks.load(is, keyStorePassword);
      } finally {
         Util.close(is);
      }
   }

}
