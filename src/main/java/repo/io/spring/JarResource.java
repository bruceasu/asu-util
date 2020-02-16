/**
 * JarResources: JarResoruces maps all resources include in a zip
 * or jar file. Additionaly, it provides a method to extract one as 
 * a blob.
 */
package repo.io.spring;

import java.io.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * @author suhanjun
 *
 */
public class JarResource {
    /**
     * htSuzes jar resource mapping tables
     */
    private Hashtable<String, Long> htSizes = new Hashtable<String, Long>();
    
    private Hashtable<String, byte[]> htJarContents = new Hashtable<String, byte[]>();
    
    /**
     * jarFileName a jar file
     */
    private String jarFileName;
    
    /**
     * Creates a JarResource. It extracts all resources from a jar 
     * into an internal Hashtable, keyed by resource names.
     * @param jarFileName j jar or zip file
     */
    public JarResource(String jarFileName) {
        this.jarFileName = jarFileName;
        init();
    }
    
    
    /**
     * Extracts a jar resource as a blob.
     * @param name a resource name.
     * @return blob data
     */
    public byte[] getResource(String name) {
         return (byte[]) htJarContents.get(name);
     }
    
    /**
     * Initalizes internal hash table with jar file resources.
     */
    private void init() {
    	ZipInputStream zis = null;
    	BufferedInputStream bis = null;
    	FileInputStream fis = null;
    	
        try {
            /* extracts just sizes only. */
            ZipFile zf = new ZipFile(this.jarFileName);
            Enumeration<? extends ZipEntry> e = zf.entries();
            while (e.hasMoreElements()) {
                ZipEntry ze = (ZipEntry)e.nextElement();
                htSizes.put(ze.getName(), ze.getSize());
            }
            zf.close();
            
            /* extract resources and put them into the hashtable */
            fis = new FileInputStream(this.jarFileName);
            bis = new BufferedInputStream(fis);
            zis = new ZipInputStream(bis);
            ZipEntry ze = null;
            while ((ze=zis.getNextEntry()) != null) {
                if (ze.isDirectory()) {
                    continue;
                }
                long size = ze.getSize();
                if (size == -1) {
                    size = htSizes.get(ze.getName());
                }
                /* 文件估计最多不会2G */
                byte[] b = new byte[(int)size];
                int rb = 0;
                int chunk = 0;
                while ((size - rb) > 0) {
                    chunk = zis.read(b, rb, (int)size - rb);
                    if (chunk == -1) {
                        break;
                    }
                    rb += chunk;
                } 
                /* add to inernal resource hash table */
                htJarContents.put(ze.getName(), b);

            }
        } catch (NullPointerException e) {
           e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        	JarResource.closeQuietly(zis);
        	JarResource.closeQuietly(bis);
        	JarResource.closeQuietly(fis);
        }
    }
    
    private static void closeQuietly(InputStream is) {
    	if (is != null) {
    		try {
				is.close();
			} catch (IOException e) {
				// 关闭失败也不提示
			}
    	}
    }
    
    /**
     * Dump a zuo entry into a string.
     * @param ze a ZipEntry
     * @return a String
     */
    private String dumpZipEntry(ZipEntry ze) {
        StringBuffer sb = new StringBuffer();
        if (ze.isDirectory()) {
            sb.append("d\t");
        } else {
            sb.append("f\t" );
        }
        if (ze.getMethod() == ZipEntry.STORED) {
            sb.append("store\t");
        } else {
            sb.append("defaulted\t");
        }
        sb.append(ze.getName());
        sb.append("\t");
        sb.append(ze.getSize());
        if (ze.getMethod() == ZipEntry.DEFLATED) {
            sb.append("/" + ze.getCompressedSize());
        }
        return (sb.toString());
    }
    
    
    /**
     * Is a test driver. Given a jar file and a reource name, it
     * trys to extract the resource and then tells us whether it
     * could or not.
     * 
     *  <strong> Excample </strong>
     *  Let's say you have a jar file which jarred up a bunch of 
     *  gif image files. Nowm by using JarResource, you could 
     *  extract, create, and display those images on-the-fly.
     *  
     *  <pre>
     *      ...
     *      JarResource jr = new JarResource("GifBundle.jar");
     *      Image image = Toolkit.createImage(jr.getResource("logo.gif"));
     *      Image image = Toolkit.getDefaultToolkit().createImage(jr.getResource("logo.gif"));
     *      ...
     *  </pre>
     *  
     * @param args 参数
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.exit(1);
        }
        JarResource jr = new JarResource(args[0]);
        byte[] buff = jr.getResource(args[1]);
        if (buff == null) {
            System.out.println("Could not find " + args[1] + ".");
        } else {
            System.out.println("Found " + args[1] + " (length=" + buff.length + ").");
        }
    }

}
