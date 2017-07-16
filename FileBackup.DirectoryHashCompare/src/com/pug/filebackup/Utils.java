package com.pug.filebackup;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class Utils {
	
	public static String hashAlgorithm = null; // ex. SHA-512

	public static void getList(File file, Map<String, String>hashList, String defaultPath) throws IOException {
		
		if( file.isDirectory() ) {
			File []ls = file.listFiles();
			if( ls == null ) {
				throw new IOException("Can not load files: " + file.getPath());
			}
			if( ls.length >= 0 ) { // Access 권한관련, 빈폴더 관련 (하위 디렉토리 접근권한 없으면(is null) ingore)
				for( File f : ls ) {
					getList(f, hashList, defaultPath);
				}
			}
			
		} else if( file.isFile() ) {
			try {
				String path = file.getPath();
				String hash = getFileHash(file);
				
				if( path.indexOf(defaultPath) == 0 )
				{
					path = path.substring(defaultPath.length());
				}
				
				hashList.put(path, hash);
			} catch( NoSuchAlgorithmException e ) {
				e.printStackTrace();
			}
		}
		
	}

	public static String getFileHash(final File target) throws NoSuchAlgorithmException, IOException
	{
		// 출처: http://reiphiel.tistory.com/entry/about-security-file-hash-checksum
		
	    final MessageDigest md = MessageDigest.getInstance(hashAlgorithm);
	    InputStream is = null;
	    
	    try
	    {
	        is = new FileInputStream(target);
	        byte[] buffer = new byte[1024];
	        int readBytes = 0;
	 
	        while ((readBytes = is.read(buffer)) > -1)
	        {
	            md.update(buffer, 0, readBytes);
	        }
	 
	        StringBuilder builder = new StringBuilder();
	        byte[] digest = md.digest();
	        for (byte b : digest)
	        {
	            builder.append(Integer.toHexString(0xff & b));
	        }
	        
	        return builder.toString();
	        
	    }
	    finally
	    {
	        if (is != null)
	        {
	            try
	            {
	                is.close();
	            }
	            catch (IOException e)
	            {
	                e.printStackTrace();
	            }
	        }
	    }
	    
	    /*
    	사용예제
    	getFileHash("SHA-512", new File("D:/FileZilla_Server-0_9_49.exe"));
    	*/
	}


}
