package ru.sberbank.qlik.sense;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.websockets.Base64Utils;
import ru.sberbank.qlik.sense.methods.BaseRequest;
import ru.sberbank.qlik.sense.methods.BaseResponse;
import ru.sberbank.qlik.services.QlikApiUtils;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.websocket.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class QlikSenseClient {
    private static final Logger log = LogManager.getLogger(QlikSenseClient.class);
    private final String serverHost;
    private final URI websocketURI;
    private boolean connected;
    private Session session;
    private AtomicInteger id = new AtomicInteger();
    private Map<Integer, MethodCall> calls = Collections.synchronizedMap(new HashMap<Integer, MethodCall>());
    private ExecutorService executor;
    private SSLContext sslContext;
    private File rootCertificate;
    private File clientCertificate;
    private File clientKeyPath;
    private String clientKeyPassword;
    private String user;
    private String domain;

    public QlikSenseClient(String serverHost, int serverPort, String apiContext, File rootCertificatePath, File clientCertificatePath, File clientKeyPath, String clientKeyPassword, String qlikUser, String qlikDomain) throws URISyntaxException {
        this.serverHost = serverHost;
        this.websocketURI = new URI("wss", null, this.serverHost, serverPort, apiContext, null, null);
        this.rootCertificate = rootCertificatePath;
        this.clientCertificate = clientCertificatePath;
        this.clientKeyPath = clientKeyPath;
        this.clientKeyPassword = clientKeyPassword;
        this.user = qlikUser;
        this.domain = qlikDomain;
    }

    public boolean isConnected() {
        return connected;
    }

    void setConnected(boolean connected) {
        log.debug("Qlik Client " + (connected ?  "connected" : "disconnected"));
        this.connected = connected;
    }

    public void connect() {
        log.debug("Client connecting...");
        String keystorePassword = "";

        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);

            Certificate clientCertificate = setCertificate(cf, keyStore, this.clientCertificate.toString(), "client");
            Certificate rootCertificate = setCertificate(cf, keyStore, this.rootCertificate.toString(), "root");

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());

            char[] keystorePasswordChars = keystorePassword.toCharArray();
            KeyStore keyKeystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyKeystore.load(null, keystorePasswordChars);

            Certificate[] certificates = {clientCertificate, rootCertificate};
            setKey(keyKeystore, clientKeyPath.toString(), "client_key", clientKeyPassword, certificates);

            kmf.init(keyKeystore, keystorePasswordChars);
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);

            ClientManager websocketClient = ClientManager.createClient();
            this.sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            websocketClient.getProperties().put("org.glassfish.tyrus.client.sslEngineConfigurator", new SSLEngineConfigurator(sslContext));

            ClientEndpointConfig config = ClientEndpointConfig.Builder.create()
                    .configurator(new Configurator(this.domain, this.user))
                    .build();

            session = websocketClient.connectToServer(new EndpointHandler(this), config, websocketURI);

            this.executor = Executors.newCachedThreadPool();

            setConnected(true);
        } catch (Exception e) {
            log.error(e);
            setConnected(false);
        }
    }

    private Certificate setCertificate(CertificateFactory cf, KeyStore keyStore, String name, String server) throws CertificateException, FileNotFoundException, KeyStoreException {
        Certificate serverCertificate = cf.generateCertificate(new FileInputStream(name));
        keyStore.setCertificateEntry(server, serverCertificate);
        return serverCertificate;
    }

    private void setKey(KeyStore keyKeystore, String file, String alias, String password, Certificate[] certificates) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, KeyStoreException {
        StringBuilder sb = new StringBuilder();
        String s1;
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        try {
            while ((s1 = bufferedReader.readLine()) != null) {
                if (!s1.startsWith("-")) {
                    sb.append(s1);
                }
            }
        } finally {
            bufferedReader.close();
        }
        byte[] decoded = Base64Utils.decode(sb.toString());
        KeyFactory rsa = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(decoded);
        PrivateKey key = rsa.generatePrivate(pkcs8EncodedKeySpec);
        keyKeystore.setKeyEntry(alias, key, password.toCharArray(), certificates);
    }

    public void disconnect() throws IOException {
        this.executor.shutdown();
        session.close();
        log.debug("Client disconnected");
    }

    public <R, T extends BaseRequest<R>> Future<T> callAsync(T method) {
        int id = this.id.incrementAndGet();
        method.setId(id);
        MethodCall methodCall = new MethodCall<R, T>(method, session);
        calls.put(id, methodCall);
        return executor.submit(methodCall);
    }

    public <R, T extends BaseRequest<R>> T call(T method) throws Exception {
        return callAsync(method).get();
    }

    public static ObjectMapper getObjectMapper() {
        return QlikApiUtils.getObjectMapper();
    }

    public static class EndpointHandler extends Endpoint {
        private final QlikSenseClient client;

        public EndpointHandler(QlikSenseClient client) {
            this.client = client;
        }

        @Override
        public void onClose(Session session, CloseReason closeReason) {
            log.info("Close session" + closeReason);
            this.client.setConnected(false);
        }

        @Override
        public void onError(Session session, Throwable thr) {
            log.error(thr.getMessage(), thr);
        }

        void onMessage(String message, Session session) throws IOException {
            ObjectMapper objectMapper = getObjectMapper();
            BaseResponse baseResponse = objectMapper.readValue(message, BaseResponse.class);
            Integer id = baseResponse.getId();
            if (id != null) {
                MethodCall call = this.client.calls.remove(id);
                if (call != null) {
                    Class resultType = call.getRequest().getResponseType();
                    call.setResponse(objectMapper.readValue(message, resultType));
                    log.debug(call.getRequest().getId() + " : " + call.getDuration() + " <-- " + message);
                } else {
                    log.warn("Unexpected id:" + message);
                }
            } else {
                log.warn("Undefined message:" + message);
            }
        }

        @Override
        public void onOpen(final Session session, EndpointConfig config) {
            log.debug("Open session");
            final EndpointHandler handler = this;
            session.addMessageHandler(new MessageHandler.Partial<String>() {
                private StringBuffer buffer = new StringBuffer();

                @Override
                public void onMessage(String partialMessage, boolean last) {
                    buffer.append(partialMessage);
                    if (last) {
                        try {
                            handler.onMessage(buffer.toString(), session);
                            buffer.setLength(0);
                        } catch (IOException e) {
                            log.error(e);
                        }
                    }
                }
            });
        }
    }
}
