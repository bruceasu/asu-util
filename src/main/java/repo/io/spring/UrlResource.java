package repo.io.spring;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;


public class UrlResource extends AbstractResource {

    public UrlResource(URL url) {
        Objects.requireNonNull(url);
        this.url = url;
        cleanedUrl = getCleanedUrl(this.url, url.toString());
    }

    public UrlResource(String path) throws MalformedURLException {
        Objects.requireNonNull(path);
        url = new URL(path);
        cleanedUrl = getCleanedUrl(url, path);
    }

    private URL getCleanedUrl(URL originalUrl, String originalPath) {
        try {
            return new URL(originalPath);
        } catch (MalformedURLException e) {
            return originalUrl;
        }

    }

    @Override
    public InputStream getInputStream() throws IOException {
        URLConnection con = url.openConnection();
        con.setUseCaches(false);
        return con.getInputStream();
    }

    @Override
    public URL getURL() throws IOException {
        return url;
    }

    @Override
    public File getFile() throws IOException {
        return new File(url.getFile());
    }

    @Override
    public IResource createRelative(String relativePath) throws MalformedURLException {
        if (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }
        return new UrlResource(new URL(url, relativePath));
    }

    @Override
    public String getFilename() {
        return (new File(url.getFile())).getName();
    }

    @Override
    public String getDescription() {
        return "URL [" + url + "]";
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || (obj instanceof UrlResource) && cleanedUrl
                .equals(((UrlResource) obj).cleanedUrl);
    }

    @Override
    public int hashCode() {
        return cleanedUrl.hashCode();
    }

    private final URL url;

    private final URL cleanedUrl;


}
