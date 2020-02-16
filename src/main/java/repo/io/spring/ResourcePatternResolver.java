
/**
 * 
 */
package repo.io.spring;

import java.io.IOException;


/**
 * @author Suk Honzeon
 *
 */
public abstract class ResourcePatternResolver implements IResourceResolver {
	public abstract IResource[] getResources(String s) throws IOException;

	public static final String CLASSPATH_ALL_URL_PREFIX = "classpath*:";
}
