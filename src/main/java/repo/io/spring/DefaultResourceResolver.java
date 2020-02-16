package repo.io.spring;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

public class DefaultResourceResolver implements IResourceResolver {

    public DefaultResourceResolver() {
        classLoader = Thread.currentThread().getContextClassLoader();
    }

    public DefaultResourceResolver(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public IResource getResource(String location) {
        Objects.requireNonNull(location);
        if (location.startsWith("classpath:")) {
            return new ClassPathResource(location.substring("classpath:".length()),
                    getClassLoader());
        }
        URL url;
        try {
            url = new URL(location);
            return new UrlResource(url);
        } catch (MalformedURLException e) {
            return getResourceByPath(location);
        }
    }

    protected IResource getResourceByPath(String path) {
        return new ClassPathResource(path, getClassLoader());
    }

    private ClassLoader classLoader;
}
