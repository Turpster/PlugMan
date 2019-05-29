package com.rylinaux.plugman.util;

/*
 * #%L
 * PlugMan
 * %%
 * Copyright (C) 2010 - 2019 PlugMan
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */


import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;

/**
 * Dealing with http(s) downloads.
 *
 * @author turpster
 */
public class HttpDownload
{
    /**
     * The URL of the download.
     */
    private String downloadURL = null;

    /**
     * The total bytes of the download.
     */
    private long totalBytes = 0;

    /**
     * The total bytes that have been downloaded.
     */
    private volatile long downloadedBytes = 0;

    /**
     * The directory where the file will be placed.
     */
    private String folderLocation = null;

    /**
     * The name of the file.
     */
    @Nullable
    private String fileName = null;

    /**
     * A Getter for the downloadURL.
     *
     * @return the url of the download
     */
    public String getDownloadURL() {
        return downloadURL;
    }

    /**
     * A Getter for totalBytes.
     *
     * @return the total bytes of the download
     */
    public long getTotalBytes() {
        return totalBytes;
    }

    /**
     * A Getter for downloadedBytes.
     *
     * @return the total bytes that have been downloaded.
     */
    public long getDownloadedBytes() {
        return downloadedBytes;
    }

    /**
     * Get the path of the downloaded file.
     *
     * @return The path of the downloaded file.
     */
    @Nullable
    public String getTargetLocation()
    {
        if (fileName != null)
            return folderLocation + "/" + fileName;
        return null;
    }

    /**
     * A Getter for targetFolder.
     *
     * @return The directory where the file will be placed.
     */
    public String getTargetFolder()
    {
        return folderLocation.toString();
    }


    /**
     * Get the percentage of the completed download.
     *
     * @return percentage of the completed download.
     */
    public float getPercentage()
    {
        if (this.getTotalBytes() != 0f)
            return (float) this.getDownloadedBytes() / this.getTotalBytes();
        return 0;
    }

    /**
     * Construct out object.
     *
     * @param downloadURL The URL of the download.
     * @param folderLocation The download folder.
     * @param fileName The name of the file; null if default download name.
     */
    public HttpDownload(String downloadURL, String folderLocation, @Nullable String fileName)
    {
        this.downloadURL = downloadURL;
        this.folderLocation = folderLocation;
        this.fileName = fileName;
    }

    public HttpDownload(String downloadURL, String folderLocation)
    {
        this.downloadURL = downloadURL;
        this.folderLocation = folderLocation;
    }


    /**
     * Download the file.
     *
     * @return The file that has been downloaded.
     * @throws FileAlreadyExistsException should the target file already exist.
     * @throws IOException should there be a failure inputting and outputting data - through the internet or locally.
     */
    public File download() throws FileAlreadyExistsException, IOException
    {
        URL url = new URL(this.getDownloadURL());

        // Loop to set the redirect URL if the response is HTTP_MOVED.
        while (true) {
            HttpURLConnection httpURLConnection = null;
            try {
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("HEAD");

                switch (httpURLConnection.getResponseCode())
                {
                    // TODO Add more and throw exceptions.
                    case HttpURLConnection.HTTP_MOVED_PERM:
                    case HttpURLConnection.HTTP_MOVED_TEMP:
                        url = new URL(httpURLConnection.getHeaderField("Location"));
                        continue;
                    default:
                }

                totalBytes = httpURLConnection.getContentLengthLong();
                System.out.println(totalBytes);
                if (fileName == null) {

                    System.out.println(httpURLConnection.getHeaderFields().toString());
                    fileName = httpURLConnection.getHeaderField("Content-Disposition").replaceFirst("(?i)^.*filename=\"?([^\"]+)\"?.*$", "$1");
                }
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            }
            break;
        }

        File file = new File(folderLocation + "/" + fileName);

        if (file.isFile())
        {
            throw new FileAlreadyExistsException(file.getAbsolutePath());
        }

        file.createNewFile();

        BufferedInputStream downloadStream = new BufferedInputStream(url.openStream());
        BufferedOutputStream pluginFileStream = new BufferedOutputStream(new FileOutputStream(file));

        int inByte;
        while ((inByte = downloadStream.read()) != -1) {
            pluginFileStream.write(inByte);
            downloadedBytes += 1;
        }

        downloadStream.close();
        pluginFileStream.close();

        return file;
    }
}
