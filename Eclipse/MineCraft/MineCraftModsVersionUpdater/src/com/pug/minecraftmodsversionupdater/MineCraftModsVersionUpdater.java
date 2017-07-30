package com.pug.minecraftmodsversionupdater;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MineCraftModsVersionUpdater
{
	final static String configPropertiesFilePath = "config.properties";

	public static void main(String[] args)
	{
		System.out.println("MineCraft - Mods Version Updater - v1.0");
		
		// set propertys
		
		Properties prop = new Properties();
		try
		{
			final FileInputStream iStream = new FileInputStream(configPropertiesFilePath);
			prop.load(iStream);
			iStream.close();	
		}
		catch( Exception e )
		{
			e.printStackTrace();
			return;
		}
		
		// input = mods path, curseforge project url(ex. autoreglib in "https://minecraft.curseforge.com/projects/autoreglib/files")
		
		if( args.length < 2 )
		{
			System.out.println("input = mods path, curseforge project url list(ex. autoreglib in \"https://minecraft.curseforge.com/projects/autoreglib/files\")");
			return;
		}
		
		final String modsPath = args[0];
		final ArrayList<String> curseforgeProjectUrlList = new ArrayList<String>();
		
		int i;
		
		for( i=1; i<args.length; i++ )
		{
			curseforgeProjectUrlList.add(args[i]);	
		}
		
		// print mods list from path
		
		final ArrayList<String> modList = getModList(modsPath); 
		
		if( modList == null )
		{
			System.err.println("mods folder is not directory.");
			return;
		}
		
		for( i=0; i<modList.size(); i++ )
		{
			System.out.println(modList.get(i));
		}
		
		System.out.println(modList.size() + "mods.");
		System.out.println();
		
		// get curseforge filelist from curseforgeProjectUrlList.
		
		ArrayList<String> urlList = new ArrayList<String>();
		
		for( i=0; i<curseforgeProjectUrlList.size(); i++ )
		{
			String url = getUrl(prop, curseforgeProjectUrlList.get(i));
			urlList.add(url);
			System.out.println((i+1) + ": " + url);
		}
		
		for( i=0; i<urlList.size(); i++ )
		{
			String url = urlList.get(i);
			System.out.println((i+1) + ": " + parse(prop, modList, url));
		}
		
	}
	
	private static String parse(Properties prop, ArrayList<String> modList, String url)
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
		
		String rType = null, rFilename = null;
		Elements recoads = doc.select(".project-file-list-item");
		
		for( i=0; i<recoads.size(); i++ )
		{
			String type, filename;
			Element e = recoads.get(i);
			
			type = e.select(".project-file-release-type > div").attr("class");
			type = type.substring(0, type.indexOf('-'));
			filename = e.select(".project-file-name-container a:nth-child(1)").text();
			
			if( type.equals("release") )
			{
				rType = type;
				rFilename = filename;
				break;
			} // �ݸ�, alpha�� beta�� release�� �ִ��� �� ���� �Ѵ�.
			
			if( type.equals("beta") )
			{
				rType = type;
				rFilename = filename;
			}
			else if( type.equals("alpha") )
			{
				if( rType == null || !rType.equals("beta") )
				{
					rType = type;
					rFilename = filename; // ���� filename�� ����Ʈ���� ���� �������� filename�� �ƴ϶� �׳� �̸����� ������ ��찡 �ִ�.
				}
			}
		}
		
		// search same filename in modList // TODO �˻� �˰����� ȿ��������...
		
		String prefix = "";
		
		for( i=0; i<modList.size(); i++ )
		{
			if( modList.get(i).equals(rFilename) )
			{
				prefix = "(samefile in mods directory) ";
			}
		}
	
		return prefix + rType + ": " + rFilename; // TODO null, null�� ������ ��찡 ���� �ʳ�?
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
