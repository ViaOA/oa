package com.viaoa.jsp;

import java.io.OutputStream;

/**
 * Support for components that submit multipart.
 * @author vvia
 *
 */
public interface OAJspMultipartInterface extends java.io.Serializable{
    
    OutputStream getOutputStream(int length, String originalFileName);
    
    
}
