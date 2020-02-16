
/**
 * 
 */
package repo.io.spring;


/**
 * @author Suk Honzeon
 *
 */
public interface IResourceResolver {
	IResource getResource(String s);

	String CLASSPATH_URL_PREFIX = "classpath:";
}
