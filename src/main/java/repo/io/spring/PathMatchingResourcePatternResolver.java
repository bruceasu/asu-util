package repo.io.spring;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import me.asu.util.ClassUtils;
import me.asu.util.Strings;


public class PathMatchingResourcePatternResolver extends ResourcePatternResolver {

    public PathMatchingResourcePatternResolver() {
        this(((IResourceResolver) (new DefaultResourceResolver())), null);
    }

    public PathMatchingResourcePatternResolver(ClassLoader classLoader) {
        this(((IResourceResolver) (new DefaultResourceResolver(classLoader))), classLoader);
    }

    public PathMatchingResourcePatternResolver(IResourceResolver resourceLoader) {
        this(resourceLoader, null);
    }

    public PathMatchingResourcePatternResolver(IResourceResolver resourceLoader,
                                               ClassLoader classLoader) {
        pathMatcher = new AntPathMatcher();
        Objects.requireNonNull(resourceLoader);
        this.resourceLoader = resourceLoader;
        this.classLoader = classLoader == null ? ClassUtils.getDefaultClassLoader() : classLoader;
    }


    /**
     * @return the pathMatcher
     */
    public PathMatcher getPathMatcher() {
        return pathMatcher;
    }

    /**
     * @param pathMatcher the pathMatcher to set
     */
    public void setPathMatcher(PathMatcher pathMatcher) {
        this.pathMatcher = pathMatcher;
    }

    /**
     * @return the resourceLoader
     */
    public IResourceResolver getResourceLoader() {
        return resourceLoader;
    }

    @Override
    public IResource getResource(String location) {
        return getResourceLoader().getResource(location);
    }

    @Override
    public IResource[] getResources(String locationPattern) throws IOException {
        Objects.requireNonNull(locationPattern);
        if (locationPattern.startsWith("classpath*:")) {
            if (getPathMatcher().isPattern(locationPattern.substring("classpath*:".length()))) {
                return findPathMatchingResources(locationPattern);
            } else {
                return findAllClassPathResources(locationPattern.substring("classpath*:".length()));
            }
        }
        if (getPathMatcher().isPattern(locationPattern)) {
            return findPathMatchingResources(locationPattern);
        } else {
            return (new IResource[]{getResourceLoader().getResource(locationPattern)});
        }
    }

    protected IResource[] findAllClassPathResources(String location) throws IOException {
        String path = location;
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        Enumeration resourceUrls = getClassLoader().getResources(path);
        Set result = new LinkedHashSet(16);
        URL url;
        for (; resourceUrls.hasMoreElements(); result.add(new UrlResource(url))) {
            url = (URL) resourceUrls.nextElement();
        }

        return (IResource[]) (IResource[]) result.toArray(new IResource[result.size()]);
    }

    protected IResource[] findPathMatchingResources(String locationPattern) throws IOException {
        String rootDirPath = determineRootDir(locationPattern);
        String subPattern = locationPattern.substring(rootDirPath.length());
        IResource rootDirResources[] = getResources(rootDirPath);
        Set result = new LinkedHashSet(16);
        for (int i = 0; i < rootDirResources.length; i++) {
            IResource rootDirResource = rootDirResources[i];
            if (isJarResource(rootDirResource)) {
                result.addAll(doFindPathMatchingJarResources(rootDirResource, subPattern));
            } else {
                result.addAll(doFindPathMatchingFileResources(rootDirResource, subPattern));
            }
        }

        return (IResource[]) (IResource[]) result.toArray(new IResource[result.size()]);
    }

    protected String determineRootDir(String location) {
        int prefixEnd = location.indexOf(":") + 1;
        int rootDirEnd;
        for (rootDirEnd = location.length(); rootDirEnd > prefixEnd
                && getPathMatcher().isPattern(location.substring(prefixEnd, rootDirEnd));
                rootDirEnd = location
                        .lastIndexOf('/', rootDirEnd - 2) + 1) {
            ;
        }
        if (rootDirEnd == 0) {
            rootDirEnd = prefixEnd;
        }
        return location.substring(0, rootDirEnd);
    }

    protected boolean isJarResource(IResource resource) throws IOException {
        String protocol = resource.getURL().getProtocol();
        return "jar".equals(protocol) || "zip".equals(protocol) || "wsjar".equals(protocol);
    }

    protected Set doFindPathMatchingJarResources(IResource rootDirResource, String subPattern)
            throws IOException {
        java.net.URLConnection con = rootDirResource.getURL().openConnection();
        JarFile jarFile = null;
        String jarFileUrl = null;
        String rootEntryPath = null;
        if (con instanceof JarURLConnection) {
            JarURLConnection jarCon = (JarURLConnection) con;
            jarFile = jarCon.getJarFile();
            jarFileUrl = jarCon.getJarFileURL().toExternalForm();
            rootEntryPath = jarCon.getJarEntry().getName();
        } else {
            String urlFile = rootDirResource.getURL().getFile();
            int separatorIndex = urlFile.indexOf("!/");
            jarFileUrl = urlFile.substring(0, separatorIndex);
            if (jarFileUrl.startsWith("file:")) {
                jarFileUrl = jarFileUrl.substring("file:".length());
            }
            jarFile = new JarFile(jarFileUrl);
            jarFileUrl = "file:" + jarFileUrl;
            rootEntryPath = urlFile.substring(separatorIndex + "!/".length());
        }
        if (!rootEntryPath.endsWith("/")) {
            rootEntryPath = rootEntryPath + "/";
        }
        Set result = new LinkedHashSet(8);
        Enumeration entries = jarFile.entries();
        do {
            if (!entries.hasMoreElements()) {
                break;
            }
            JarEntry entry = (JarEntry) entries.nextElement();
            String entryPath = entry.getName();
            if (entryPath.startsWith(rootEntryPath)) {
                String relativePath = entryPath.substring(rootEntryPath.length());
                if (getPathMatcher().match(subPattern, relativePath)) {
                    result.add(rootDirResource.createRelative(relativePath));
                }
            }
        } while (true);
        return result;
    }

    protected Set doFindPathMatchingFileResources(IResource rootDirResource, String subPattern)
            throws IOException {
        File rootDir = rootDirResource.getFile().getAbsoluteFile();
        Set matchingFiles = retrieveMatchingFiles(rootDir, subPattern);
        Set result = new LinkedHashSet(matchingFiles.size());
        File file;
        for (Iterator it = matchingFiles.iterator(); it.hasNext();
                result.add(new FileSystemResource(file))) {
            file = (File) it.next();
        }

        return result;
    }

    protected Set retrieveMatchingFiles(File rootDir, String pattern) throws IOException {
        if (!rootDir.isDirectory()) {
            throw new IllegalArgumentException(
                    "'rootDir' parameter [" + rootDir + "] does not denote a directory");
        }
        String fullPattern = rootDir.getAbsolutePath().replace(File.separator, "/");
        if (!pattern.startsWith("/")) {
            fullPattern = fullPattern + "/";
        }
        fullPattern = fullPattern + pattern.replace(File.separator, "/");
        Set result = new LinkedHashSet(8);
        doRetrieveMatchingFiles(fullPattern, rootDir, result);
        return result;
    }

    protected void doRetrieveMatchingFiles(String fullPattern, File dir, Set result)
            throws IOException {
        File dirContents[] = dir.listFiles();
        if (dirContents == null) {
            throw new IOException(
                    "Could not retrieve contents of directory [" + dir.getAbsolutePath() + "]");
        }
        boolean dirDepthNotFixed = fullPattern.indexOf("**") != -1;
        for (int i = 0; i < dirContents.length; i++) {
            String currPath = dirContents[i].getAbsolutePath()
                    .replace(File.separator, "/");
            if (dirContents[i].isDirectory()
                    && (dirDepthNotFixed || Strings.countOccurrencesOf(currPath, "/") < Strings
                    .countOccurrencesOf(fullPattern, "/"))) {
                doRetrieveMatchingFiles(fullPattern, dirContents[i], result);
            }
            if (getPathMatcher().match(fullPattern, currPath)) {
                result.add(dirContents[i]);
            }
        }

    }


    private static final String URL_PROTOCOL_JAR = "jar";

    private static final String URL_PROTOCOL_ZIP = "zip";

    private static final String URL_PROTOCOL_WSJAR = "wsjar";

    private static final String JAR_URL_SEPARATOR = "!/";

    private final IResourceResolver resourceLoader;

    private ClassLoader classLoader;

    private PathMatcher pathMatcher;

    /**
     * @return the classLoader
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * @param classLoader the classLoader to set
     */
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}
