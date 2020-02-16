package repo.io.spring;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;


public abstract class AbstractResource implements IResource {

	public AbstractResource() {
	}

	public boolean exists()
    {
		try {
			return getFile().exists();
		} catch (IOException ex) {
			InputStream is = null;
			try {
				is = getInputStream();
				is.close();
				return true;
			} catch (IOException e) {
				return false;
			}
		}
    }

	public boolean isOpen() {
		return false;
	}

	public URL getURL() throws IOException {
		throw new FileNotFoundException(getDescription() + " cannot be resolved to URL");
	}

	public File getFile() throws IOException {
		throw new FileNotFoundException(getDescription() + " cannot be resolved to absolute file path");
	}

	public IResource createRelative(String relativePath) throws IOException {
		throw new FileNotFoundException("Cannot create a relative resource for " + getDescription());
	}

	public String getFilename() throws IllegalStateException {
		throw new IllegalStateException(getDescription() + " does not carry a filename");
	}

	public abstract String getDescription();

	public String toString() {
		return getDescription();
	}

	public boolean equals(Object obj) {
		return obj == this || (obj instanceof IResource) && ((IResource) obj).getDescription().equals(getDescription());
	}

	public int hashCode() {
		return getDescription().hashCode();
	}

}
