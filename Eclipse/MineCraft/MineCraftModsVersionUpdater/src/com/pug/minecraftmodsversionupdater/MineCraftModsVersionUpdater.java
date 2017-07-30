package com.pug.minecraftmodsversionupdater;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.net.ssl.HttpsURLConnection;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MineCraftModsVersionUpdater
{
	final static String configPropertiesFilePath = "config.properties";
	final static String encoding = "UTF-8";
	static String modsPath;
	static ArrayList<String> curseforgeProjectUrlList;

	public static void main(String[] args)
	{
		/*
		  v1.0: first program.
		  v1.1: More accurate get the filename and then display downloadUrl from need download mods. (But, much slowly working program)
		*/
		System.out.println("MineCraft - Mods Version Updater - v1.1");
		
		// set Properties
		
		Properties prop;
		
		try
		{
			prop = getProperties();				
		}
		catch( FileNotFoundException e )
		{
			System.err.println("Can not found properties file: " + configPropertiesFilePath);
			return;
		}
		catch( IOException e )
		{
			System.err.println("Can not read properties file: " + configPropertiesFilePath);
			return;
		}
		
		// input = mods path, curseforge project url(ex. autoreglib in "https://minecraft.curseforge.com/projects/autoreglib/files")
		
		parseInputParameters(args);
		
		// print mods list from path
		
		final ArrayList<String> modList = getModList(modsPath); 
		
		if( modList == null )
		{
			System.err.println("mods folder is not directory.");
			return;
		}
		
		printList(modList);
		
		// get curseforge filelist from curseforgeProjectUrlList.
		
		int i;
		
		ArrayList<String> urlList = new ArrayList<String>();
		
		for( i=0; i<curseforgeProjectUrlList.size(); i++ )
		{
			String url = getUrl(prop, curseforgeProjectUrlList.get(i));
			urlList.add(url);
			System.out.println((i+1) + ": " + url);
		}
		
		// Version Check
		
		try
		{
			for( i=0; i<urlList.size(); i++ )
			{
				String url = urlList.get(i);
				System.out.println((i+1) + ": " + parse(prop, modList, url, prop.getProperty("MineCraftCurseforgeHost")));
			}	
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
		
	}
	
	
	
	private static void printList(List<String> modList)
	{
		for( int i=0; i<modList.size(); i++ )
		{
			System.out.println(modList.get(i));
		}
		
		System.out.println(modList.size() + "mods.");
		System.out.println();
	}

	private static void parseInputParameters(String[] args)
	{

		if( args.length < 2 )
		{
			System.out.println("input = mods path, curseforge project url list(ex. autoreglib in \"https://minecraft.curseforge.com/projects/autoreglib/files\")");
			return;
		}
		
		modsPath = args[0];
		curseforgeProjectUrlList = new ArrayList<String>();
		
		int i;
		
		for( i=1; i<args.length; i++ )
		{
			curseforgeProjectUrlList.add(args[i]);	
		}
		
	}

	private static Properties getProperties() throws IOException
	{
		Properties prop = new Properties();
		
		final FileInputStream iStream = new FileInputStream(configPropertiesFilePath);
		prop.load(iStream);
		iStream.close();
		
		return prop;
	}

	private static String parse(Properties prop, ArrayList<String> modList, String url, String host) throws IOException
	{ // TODO �Ƹ� ��� 1���̻��� ������ ������ ������ �Ľ̿��� ������ �� ���̴�. // TODO �� ���� �������� ���ؼ� �����Ҽ� ������ ����(1������ �Ѿ���� ���Ϲ����� ���°��� ���� �� ����, �������������� �Ѿ�� �� ������ Release�� ���µ��ؼ� �Ѿ�� �ɵ�)
		
		int i;
		Document doc;
		
		// get document from url
		
		try
		{
			doc = Jsoup.connect(url).get();
		}
		catch( IOException e )
		{
			return "Connect error: " + e.toString();
		}
		
		// get higher type and name (types = release > beta > alpha)
		
		// TODO �Ľ��� properties�� ���� ���� �����ϵ��� ����
		
		String rType = null, rFilename = null, rDownloadUrl = null;
		Elements recoads = doc.select(".project-file-list-item"); // ������ ������ ������ ��¥ ������������ ���ĵ�����.
		
		for( i=0; i<recoads.size(); i++ )
		{
			String type, downloadUrl, filename;
			Element e = recoads.get(i);
			
			type = e.select(".project-file-release-type > div").attr("class");
			type = type.substring(0, type.indexOf('-'));
			downloadUrl = host + e.select(".project-file-download-button a").attr("href");
			
			// begin - get filename
			
			HttpsURLConnection conn = (HttpsURLConnection)new URL(host + e.select(".project-file-download-button a").attr("href")).openConnection();
			conn.connect();
			conn.getResponseCode(); // �̰Ŷ� ���ϸ� Redirect�� �ȵǼ� RedirectURL�������´�, HTTP 307�϶� ��� ��ƾߵ��� �𸣰���.
			filename = conn.getURL().toString();
			filename = filename.substring(filename.lastIndexOf('/')+1);
			conn.disconnect();
			
			// end - get filename
			
			if( type.equals("release") ) // ���� ������ ���type�� ���纸�� ������ �����̴�.
			{
				rType = type;
				rFilename = filename;
				rDownloadUrl = downloadUrl;
				break; // ���̻� �̺��� ���� ������ ����.
			}
			
			if( type.equals("beta") ) // ���� ������ type�� ������beta�̰ų� alpha,release�� �� �ִ�.
			{
				if( rType == null || rType.equals("aplha") ) // �� ������������ üũ
				{
					rType = type;
					rFilename = filename;
					rDownloadUrl = downloadUrl;
				}
			}
			else if( type.equals("alpha") ) // ���� ������ type�� ������alpha�̰ų� beta,release�� �� �ִ�.
			{
				if( rType == null ) // �� ������������ üũ
				{
					rType = type;
					rFilename = filename;
					rDownloadUrl = downloadUrl;
				}
			}
		}
		
		rFilename = URLDecoder.decode(rFilename, encoding);
		
		// search same filename in modList // TODO �˻� �˰����� ȿ��������...
		
		String prefix = null;
		
		for( i=0; i<modList.size(); i++ )
		{
			if( modList.get(i).equals(rFilename) )
			{
				prefix = "(samefile in mods directory) ";
			}
		}
		
		if( prefix != null )
		{
			return prefix + rType + ": " + rFilename;
		}
		else
		{
			return rType + ": " + rFilename + ": " + rDownloadUrl;
		}
	}

	private static String getUrl(Properties prop, String projectUrl)
	{

		final String vSimbol = prop.getProperty("vSimbol");
		final String MineCraftCurseforgeFilesUrlFormat = prop.getProperty("MineCraftCurseforgeFilesUrlFormat");
		
		return MineCraftCurseforgeFilesUrlFormat.replaceAll(vSimbol, projectUrl);
	}

	public static ArrayList<String> getModList(String modsPath)
	{
		ArrayList<String> modListString = new ArrayList<String>();
		
		// get File from modsPath
		
		File modsFolder = new File(modsPath);
		if( !modsFolder.isDirectory() )
		{
			return null;
		}
	
		// check mods count and print from *.jar in File(it's directory) 
		
		File []modList = modsFolder.listFiles();
		for( File modFile : modList )
		{
			if( modFile.isFile() )
			{
				String path;
				
				path = modFile.getPath();
				path = path.substring(path.lastIndexOf('.')+1);
				
				if( path.equalsIgnoreCase("jar") )
				{
					modListString.add(modFile.getName());
				}
			}
		}
		
		return modListString;
	}

}
