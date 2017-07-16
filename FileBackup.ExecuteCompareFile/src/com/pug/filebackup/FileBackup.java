package com.pug.filebackup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class FileBackup
{

	public static void main(String[] args) throws Exception
	{
		// TODO 현재이슈: 같은 옵션으로 압축하면 CRC가 틀려진다.
		
		final String input = "C:\\vol\\game\\MineCraft\\bak\\compare.json";
		final String output = "C:\\vol\\game\\MineCraft\\bak\\test";
		
		JsonObject json = new JsonParser().parse(new FileReader(input)).getAsJsonObject();
		//System.out.println(json.toString());
		
		final String version = json.get("version").getAsString();
		System.out.println("Version: " +version);
		
		JsonElement t;
		t = json.get("filepath").getAsJsonObject().get("newDirectoryPath");
		final String pathNew = !t.isJsonNull() ? t.getAsString() : null;
		t = json.get("filepath").getAsJsonObject().get("oldDirectoryPath");
		final String pathOld = !t.isJsonNull() ? t.getAsString() : null;
		
		if( !version.equals("1.0") ) 
		{
			System.err.println("Not support version: " + version);
			return;
		}
		
		if( !new File(output).mkdirs() )
		{
			if( !new File(output).isDirectory() )
			{
				System.err.println("Can not make directorys: " + output);
				return;
			}
		}
		
		JsonArray list = json.get("files").getAsJsonArray();
		for( int i=0,len=list.size(); i<len; i++ )
		{
			JsonObject e = list.get(i).getAsJsonObject();
			String tag = e.get("tag").getAsString();
			String path = e.get("path").getAsString();
		
			if( tag.equals("add") )
			{
				System.out.println(pathNew + path);
				System.out.println(output + path);
				
				String dPath = output + path.substring(0,path.lastIndexOf(File.separator));
				new File(dPath).mkdirs();
				copyFileUsingStream(new File(pathNew + path), new File(output + path));
			}
			
			else if( tag.equals("modify") )
			{
				System.out.println(pathNew + path);
				System.out.println(output + path);
				copyFileUsingStream(new File(pathNew + path), new File(output + path));
			}
			
			else if( tag.equals("delete") )
			{
				// TODO something
			}
		}
	}

	private static void copyFileUsingStream(File source, File dest) throws IOException
	{
	    InputStream is = null;
	    OutputStream os = null;
	    
	    try
	    {
	        is = new FileInputStream(source);
	        os = new FileOutputStream(dest);
	        
	        byte[] buffer = new byte[1024];
	        int length;
	        while ((length = is.read(buffer)) > 0)
	        {
	            os.write(buffer, 0, length);
	        }
	        
	    } 
	    finally
	    {
	        is.close();
	        os.close();
	    }
	}

}
