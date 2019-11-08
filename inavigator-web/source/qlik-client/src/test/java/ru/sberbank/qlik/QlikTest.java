package ru.sberbank.qlik;

import java.io.File;

public interface QlikTest {
    String SERVER = "sbt-csit-011.ca.sbrf.ru";
    int SERVER_PORT = 4747;
    String API_CONTEXT = "/app";
    String DOCUMENT_ID = "7E129B08-CC35-4AD9-9DC9-B57E73BD455A";
    String BALANCE_TABLE_ID = "eparPnX";
    String BALANCE_BARCHAR_ID = "PspkZm";
    String CERTIFICATE_DIR = "./cerificates";
    File ROOT_CERTIFICATE = new File(CERTIFICATE_DIR, "root.pem");
    File CLIENT_CERTIFICATE = new File(CERTIFICATE_DIR, "client.pem");
    File CLIENT_KEY_PATH = new File(CERTIFICATE_DIR, "client_key_8.pem");
    String ALPHA_DOMAIN = "ALPHA";
    String USER = "sbt-biryukov-su";
    String CLIENT_KEY_PASSWORD = "";
}
