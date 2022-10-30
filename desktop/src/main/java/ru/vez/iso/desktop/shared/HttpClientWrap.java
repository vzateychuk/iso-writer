package ru.vez.iso.desktop.shared;

import com.google.gson.JsonObject;
import org.apache.http.client.methods.HttpPost;

/**
 * HttpClient wrapper
 * */
public interface HttpClientWrap {

    JsonObject postDataRequest(HttpPost httpPost);

}
