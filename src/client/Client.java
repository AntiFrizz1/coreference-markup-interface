package client;

import document.Document;

import java.net.Socket;

/**
 * 
 */
public interface Client {
    void sendUpdates(Document document);
    void sendInfo(Document document);
    String getInfo();
    void join(int who); // 0 - for users, 1 - for judges;
    void listener();
    void close();
}
