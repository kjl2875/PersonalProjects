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
	{ // TODO 아마 적어도 1개이상의 파일이 나오지 않으면 파싱에서 오류가 날 것이다. // TODO 더 많은 페이지에 대해서 지원할수 있으면 지원(1페이지 넘어가도록 동일버전이 나온경우는 없는 것 같고, 다음페이지까지 넘어가야 될 정도의 Release가 없는듯해서 넘어가도 될듯)
		
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
		
		// TODO 파싱을 properties를 통해 변경 가능하도록 개발
		
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
			} // 반면, alpha나 beta는 release가 있는지 더 봐야 한다.
			
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
					rFilename = filename; // 현재 filename이 사이트에서 나온 값때문에 filename이 아니라 그냥 이름으로 나오는 경우가 있다.
				}
			}
		}
		
		// search same filename in modList // TODO 검색 알고리즘좀 효율적으로...
		
		String prefix = "";
		
		for( i=0; i<modList.size(); i++ )
		{
			if( modList.get(i).equals(rFilename) )
			{
				prefix = "(samefile in mods directory) ";
			}
		}
	
		return prefix + rType + ": " + rFilename; // TODO null, null이 나오는 경우가 없지 않나?
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
