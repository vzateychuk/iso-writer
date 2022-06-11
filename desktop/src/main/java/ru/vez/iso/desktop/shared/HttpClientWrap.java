package ru.vez.iso.desktop.shared;

import org.apache.http.client.methods.HttpPost;

/**
 * HttpClient wrapper
 * */
public interface HttpClientWrap {

    String postDataRequest(HttpPost httpPost);

}
