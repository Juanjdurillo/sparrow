/**
 * SystemClassLoaderManager.java
 *
 * @author Antonio J. Nebro
 * @version 1.0
 */

package sparrow.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;

public class SystemClassLoaderManager {
  /**
   * Constructor
   */
  public SystemClassLoaderManager() {
  } // SystemClassLoaderManager
  
  /**
   * Adds an array of URLs to the system class loader
   * @param urls
   * @throws SecurityException
   * @throws NoSuchMethodException
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   */
  public void updateSystemClassManager(URL [] urls) throws SecurityException, 
                                                    NoSuchMethodException, 
                                                    IllegalArgumentException, 
                                                    IllegalAccessException, 
                                                    InvocationTargetException {
    
    Class classLoader = URLClassLoader.class;
    Class parameters[] = new Class[]{ URL.class };

    URLClassLoader systemClassLoader = (URLClassLoader)ClassLoader.getSystemClassLoader();
    URL[] systemUrls = systemClassLoader.getURLs() ;
    
    Method method = classLoader.getDeclaredMethod("addURL", parameters);
    method.setAccessible(true);

    // We have to check that the new URLs are not already included in the 
    // system class manager   
    boolean exists ;
    for (int i = 0; i < urls.length; i++) {
      exists = false ;
      for (int j = 0; j < systemUrls.length; j++) {
        if (urls[i].toString().compareTo(systemUrls[j].toString()) == 0) 
        exists = true ;
      } // for
      if (exists) {
        //System.out.println("URL -" + urls[i] + "- does exist") ;
        exists = false ;
      }
      else {
        //System.out.println("URL " + urls[i] + " does not exist") ;
        URL[] newUrl = new URL[1] ;
        newUrl[0] = urls[i] ;
        method.invoke(systemClassLoader, newUrl);       
      }
    } // for

  }
  
  /**
   * Get the URLs in the system class loader
   * @return
   */
  public URL[] getURLs() {

    URLClassLoader classLoader = (URLClassLoader)ClassLoader.getSystemClassLoader();
    URL[] urls = classLoader.getURLs();

    return urls ;
  }
  
  /**
   * Prints the URLs in the system class loader
   */
  public void printURLs() {
    String result ;
    URL[] urls = ((URLClassLoader)ClassLoader.getSystemClassLoader()).getURLs() ;
    
    System.out.println("Number of URLs: " + urls.length ) ;
    for (int i = 0; i < urls.length; i++) {
      System.out.println(i + ": " + urls[i]) ;
    }
  } // printResults
}

