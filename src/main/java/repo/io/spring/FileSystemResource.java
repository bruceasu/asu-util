package repo.io.spring;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Objects;

public class FileSystemResource extends AbstractResource {

	
	public FileSystemResource(File file) {
		Objects.requireNonNull(file);
		this.file = file;
		path = file.getAbsolutePath();
	}

	public FileSystemResource(String path) {
		Objects.requireNonNull(path);
		file = new File(path);
		this.path = path;
	}

	public final String getPath() {
		return path;
	}
	@Override
	public boolean exists() {
		return file.exists();
	}
	@Override
	public InputStream getInputStream() throws IOException {
		return new FileInputStream(file);
	}
	@Override
	public URL getURL() throws IOException {
		return new URL("file:" + file.getAbsolutePath());
	}

	@Override
	public File getFile() {
		return file;
	}
	@Override
	public IResource createRelative(String relativePath) {
		String pathToUse =  Paths.get(path).relativize(Paths.get(relativePath)).toString();
		return new FileSystemResource(pathToUse);
	}
	@Override
	public String getFilename() {
		return file.getName();
	}
	@Override
	public String getDescription() {
		return "file [" + file.getAbsolutePath() + "]";
	}
	@Override
	public boolean equals(Object obj) {
		return obj == this || (obj instanceof FileSystemResource) && path.equals(((FileSystemResource) obj).path);
	}
	@Override
	public int hashCode() {
		return path.hashCode();
	}

	private final File file;

	private final String path;
}
