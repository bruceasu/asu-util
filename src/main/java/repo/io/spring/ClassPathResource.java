package repo.io.spring;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Objects;
import me.asu.util.ClassUtils;

public class ClassPathResource extends AbstractResource {

    public ClassPathResource(String path) {
        this(path, (ClassLoader) null);
    }

    public ClassPathResource(String path, ClassLoader classLoader) {
        Objects.requireNonNull(path);
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        this.path = path;
        this.classLoader = classLoader == null ? ClassUtils.getDefaultClassLoader() : classLoader;
    }

    public ClassPathResource(String path, Class clazz) {
        Objects.requireNonNull(path);
        this.path = path;
        this.clazz = clazz;
    }

    protected ClassPathResource(String path, ClassLoader classLoader, Class clazz) {
        this.path = path;
        this.classLoader = classLoader;
        this.clazz = clazz;
    }

    public final String getPath() {
        return path;
    }
    @Override
    public InputStream getInputStream() throws IOException {
        InputStream is = null;
        if (clazz != null) {
            is = clazz.getResourceAsStream(path);
        } else {
            is = classLoader.getResourceAsStream(path);
        }
        if (is == null) {
            throw new FileNotFoundException(
                    getDescription() + " cannot be opened because it does not exist");
        } else {
            return is;
        }
    }
    @Override
    public URL getURL() throws IOException {
        URL url = null;
        if (clazz != null) {
            url = clazz.getResource(path);
        } else {
            url = classLoader.getResource(path);
        }
        if (url == null) {
            throw new FileNotFoundException(
                    getDescription() + " cannot be resolved to URL because it does not exist");
        } else {
            return url;
        }
    }
    @Override
    public File getFile() throws IOException {
        return new File(getURL().getFile());
    }
    @Override
    public IResource createRelative(String relativePath) {
        String pathToUse = Paths.get(path).relativize(Paths.get(relativePath)).toString();
        return new ClassPathResource(pathToUse, classLoader, clazz);
    }
    @Override
    public String getFilename() {
        return Paths.get(path).getFileName().toString();
    }
    @Override
    public String getDescription() {
        return "class path resource [" + path + "]";
    }
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof ClassPathResource) {
            ClassPathResource otherRes = (ClassPathResource) obj;
            return path.equals(otherRes.path) && classLoader.equals(otherRes.classLoader)
                    && clazz.equals(otherRes.clazz);
        } else {
            return false;
        }
    }
    @Override
    public int hashCode() {
        return path.hashCode();
    }

    private final String path;

    private ClassLoader classLoader;

    private Class clazz;
}
