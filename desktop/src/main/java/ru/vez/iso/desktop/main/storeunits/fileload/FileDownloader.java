package ru.vez.iso.desktop.main.storeunits.fileload;

public interface FileDownloader {

    /**
     * Download storage unit to directory "file-cache"
     *
     * @param url - backend API (http://host:port/api
     * @param token - authentication token
     * @param destFile - downloaded file stored
     * */
    void download(String url, String token, String destFile);

}
