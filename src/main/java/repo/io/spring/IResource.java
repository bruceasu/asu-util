package repo.io.spring;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public interface IResource {

	boolean exists();

	boolean isOpen();

	URL getURL() throws IOException;

	File getFile() throws IOException;

	IResource createRelative(String s) throws IOException;

	String getFilename();

	String getDescription();
	
	InputStream getInputStream() throws IOException;
}