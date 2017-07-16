package com.pug.filebackup;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class FileBackup
{
	public static String version = "1.0";
	
	public static final String optionValueRegex = "[ ]*([^=]*)=([^ ]*)"; // 분리자,key,value구분 잘해서 2개그룹 만들고, 해당부분 수정되면 doHelp도 업데이트 해줘야됨.
	public static boolean fStdoutData = true;
	public static boolean fStdoutConfig = true;
	public static DataType dataType = DataType.original; // null값 가능
	public static String algorithm = null; // null값 가능
	public static String spliterator = "*"; // filepath에서 사용하지 못하는 splitcator를 사용.
	
	public static void doHelp()
	{
		//Matcher m = Pattern.compile("[0-9].([0-9])").matcher(System.getProperty("java.version")); (6~8만 재대로 나옴)
		//m.find();
		final String url = "http://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#MessageDigest"; // 참고로 쓰기 충분함
		
		System.out.println("Version: " + version);
		System.out.println("args[0]: directory path");
		System.out.println("-sp string: spliterator");
		System.out.println("-i path: input from directorypath");
		System.out.println("-o path: output to filepath");
		System.out.println("-of path: -o(force)");
		System.out.println("-option [hash=" + dataType + "] | [stdoutConfig=(on|enable|true|off|disable|false)] | [stdoutData=(on|enable|true|off|disable|false)] | [type=(original|json)]: 각 옵션마다 띄어쓰기로 구분 - hash [algorithm] | standard output disable | output type original or json");
		System.out.println();
		System.out.println(String.format("sp옵션으로 분리자를 별도선택 할 수 있다.(기본값은 %s)", spliterator));
		System.out.println("i옵션이 있으면 i옵션 값이 path보다 오래된 경로로 간주하고 비교해서 출력.");
		System.out.println("o옵션이 있으면 파일로 출력.");
		System.out.println("알고리즘 참고링크 - MessageDigest Algorithms) " + url);
		System.out.println();
		System.out.println(String.format("출력내용 Original ex.) [same|add|modify|delete|(-i옵션 없으면 표시되지 않는 항목)][spliterator][path][spliterator][hash]"));
		System.out.println("출력내용 Json ex.) {\"processTimeMilliseconds\":{\"startTime\":\"2017-07-16T10:54:13.101+0000\",\"endTime\":\"2017-07-16T10:54:14.149+0000\",\"runTime\":1048},\"filepath\":{\"newDirectoryPath\":\"X:\\bak\\server_real.world_20170709_2341\",\"oldDirectoryPath\":\"X:\\bak\\server_real.world_20170629_1730\"},\"options\":{\"hash\":\"sha-512\"},\"files\":[{\"tag\":\"add\",\"path\":\"\\world\\DIM-1\\region\\r.-2.-2.mca\",\"hash\":\"3fd19fb966bee54299a4e1f1899ca5656bdcd31aab8f18ea4ea296ba927eace953ba3e13f396a41da82a3b59cc88aaae8419b2d1229ed69718a86c356feac7\"},{\"tag\":\"modify\",\"path\":\"\\world\\stats\\184b3a84-4a77-43d2-9ec4-e6f0e6b32855.json\",\"hash\":\"36a524f9ae4ca46f67a2d3e755166e5010487449ae6eb8aee40c1a2ffd6cc21c2e17b6e1e0d67f7ed88e95b0e8cdc866c199935f5cdac5441babfaeb3\"}]}");
		/*
			{
			  "processTimeMilliseconds": {
			    "startTime": "2017-07-16T10:54:13.101+0000",
			    "endTime": "2017-07-16T10:54:14.149+0000",
			    "runTime": 1048
			  },
			  "filepath": {
			    "newDirectoryPath": "X:\\bak\\server_real.world_20170709_2341",
			    "oldDirectoryPath": "X:\\bak\\server_real.world_20170629_1730"
			  },
			  "options": {
			    "hash": "sha-512"
			  },
			  "files": [
			    {
			      "tag": "add",
			      "path": "\\world\\DIM-1\\region\\r.-2.-2.mca",
			      "hash": "3fd19fb966bee54299a4e1f1899ca5656bdcd31aab8f18ea4ea296ba927eace953ba3e13f396a41da82a3b59cc88aaae8419b2d1229ed69718a86c356feac7"
			    },
			    {
			      "tag": "modify",
			      "path": "\\world\\stats\\184b3a84-4a77-43d2-9ec4-e6f0e6b32855.json",
			      "hash": "36a524f9ae4ca46f67a2d3e755166e5010487449ae6eb8aee40c1a2ffd6cc21c2e17b6e1e0d67f7ed88e95b0e8cdc866c199935f5cdac5441babfaeb3"
			    }
			  ]
			}
		*/
	}

	public static void main(String[] args) throws IOException
	{
		int i, len;
		Date t0, t1;
		long processingTime;
		
		/// parameter processing
		
		if( args.length == 0 )
		{
			doHelp();
			return;
		}
		
		File t0path = null;
		File t1path = new File(args[0]);
		File oPath = null;
		boolean oPathForceSwitch = false;
		
		for( i=1,len=args.length; i<len; i+=2 )
		{
			if( args[i].equals("-i") )
			{
				t0path = new File(args[i+1]);
			}
			
			if( args[i].equals("-o") )
			{
				oPath = new File(args[i+1]);
			}
			
			if( args[i].equals("-of") )
			{
				oPath = new File(args[i+1]);
				oPathForceSwitch = true;
			}
			
			if( args[i].equals("-sp") )
			{
				spliterator = args[i+1];
			}
			
			if( args[i].equals("-option") )
			{
				Matcher match = Pattern.compile(optionValueRegex).matcher(args[i+1]);
				while( match.find() )
				{
					String k = match.group(1);
					String v = match.group(2);
										
					// Hash 알고리즘 설정
					
					if( k.equals("hash") )
					{
						algorithm = v;
					}
					
					// stdout 설정
					
					if( k.equals("stdoutData") )
					{
						try {
							fStdoutData = getBoolean(v);	
						} catch( Exception e ) {
							System.err.println("Not support stdoutData switch: " + v);
							return;
						}
					}
					
					if( k.equals("stdoutConfig") )
					{
						try {
							fStdoutConfig = getBoolean(v);	
						} catch( Exception e ) {
							System.err.println("Not support stdoutConfig switch: " + v);
							return;
						}
					}
					
					// type 설정 (for OutputDatatype)
					
					if( k.equals("type") )
					{
						if( v.equals("original") )
						{
							dataType = DataType.original;
							continue;
						}
						
						if( v.equals("json") )
						{
							dataType = DataType.json;
							continue;
						}
						
						System.err.println("Not support datatype: " + v);
						return;
					}
				}
			}
		}
		
		if( algorithm == null )
		{
			System.err.println("Please set algorithm.");
			return;
		}
		
		if( dataType == null )
		{
			System.err.println("Please set datatype.");
			return;
		}
		
		if( t0path != null && !t0path.isDirectory() )
		{
			System.err.println("Invalid directory: " + t0path.getAbsolutePath());
			return;	
		}
		
		if( !t1path.isDirectory() )
		{
			System.err.println("Invalid directory: " + t1path.getAbsolutePath());
			return;
		}
		
		if( oPath != null && !oPathForceSwitch && oPath.isFile() )
		{
			System.err.println("Already valid file: " + oPath.getAbsolutePath());
			return;	
		}
		
		/// check start time.
		
		t0 = new Date();
		
		/// hash processing
		
		Utils.hashAlgorithm = algorithm;
		
		HashMap<String,String> t0HashList, t1HashList;
		
		if( t0path != null )
		{
			t0HashList = new HashMap<String,String>();
			Utils.getList(t0path, t0HashList, t0path.getPath());
		}
		else
		{
			t0HashList = null;
		}
		
		t1HashList = new HashMap<String,String>();
		Utils.getList(t1path, t1HashList, t1path.getPath());
		
		/// compare processing
		
		HashMap<String, String> addList = null, modifyList = null, deleteList = null;
		
		if( t0HashList != null )
		{
			// compare, result=addList,modifyList,deleteList 
			
			addList = new HashMap<String, String>();
			modifyList = new HashMap<String, String>();
			deleteList = new HashMap<String, String>();
			
			Iterator<String> iter;
			
			// roof.v1{ chk.v0 }
			
			iter = t1HashList.keySet().iterator();
			
			while( iter.hasNext() )
			{
				String k = iter.next();
				String v0 = t0HashList.get(k);
				String v1 = t1HashList.get(k);
				
				if( v0 == null )
				{
					addList.put(k, v1);
				}
				else if( !v1.equals(v0) )
				{
					modifyList.put(k, v1);
				}
			}
			
			// roof.v0{ chk.v1 }
			
			iter = t0HashList.keySet().iterator();
			
			while( iter.hasNext() )
			{
				String k = iter.next();
				if( t1HashList.get(k) == null )
				{
					deleteList.put(k, t0HashList.get(k));
				}
			}
		}
		
		/// check end time.
		
		t1 = new Date();
		
		/// set processingTime
		
		processingTime = t1.getTime() - t0.getTime();
		
		/// output processing
		
		JsonObject json = (dataType == DataType.json) ? new JsonObject() : null;
		BufferedWriter out = (oPath != null) ? new BufferedWriter(new FileWriter(oPath)) : null;
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		final String sStartTime = dateFormat.format(t0);
		final String sEndTime = dateFormat.format(t1);
		
		if( fStdoutConfig )
		{
			System.out.println("spliterator: " + spliterator);
			System.out.println("newDirectoryPath: " + t1path.getPath());
			System.out.println("oldDirectoryPath: " + ((t0path != null) ? t0path.getPath() : null));
			System.out.println("outputFilepath: " + ((oPath != null) ? oPath.getPath() : null));
			System.out.println("hash-algorithm: " + algorithm);
			System.out.println("flag-stdout: " + fStdoutData);
			System.out.println("data-type: " + dataType);
			System.out.println("Start time: " + sStartTime);
			System.out.println("End time: " + sEndTime);
			System.out.println("Processing time: " + processingTime + "ms");
		}
		
		if( out != null && json != null )
		{
			JsonObject e0;
			
			json.addProperty("version", version);
			
			e0 = new JsonObject();
			e0.addProperty("startTime", sStartTime);
			e0.addProperty("endTime", sEndTime);
			e0.addProperty("runTime", processingTime);
			json.add("processTimeMilliseconds", e0);
			
			e0 = new JsonObject();
			e0.addProperty("newDirectoryPath", t1path.getPath());
			e0.addProperty("oldDirectoryPath", (t0path != null) ? t0path.getPath() : null);
			json.add("filepath", e0);
			
			e0 = new JsonObject();
			e0.addProperty("hash", algorithm);
			json.add("options", e0);
		}
		
		Object o = (dataType == DataType.json) ? new JsonArray() : out;
		
		if( t0HashList != null ) // output for compare result case
		{
			HashMap<String, String> hmap;
			FileTag tag;
			
			hmap = addList;
			tag = FileTag.add;
			outputList(hmap, tag, o);
			
			hmap = modifyList;
			tag = FileTag.modify;
			outputList(hmap, tag, o);
			
			hmap = deleteList;
			tag = FileTag.delete;
			outputList(hmap, tag, o);
		}
		else // output for non-compare result case
		{
			outputList(t1HashList, FileTag.add, o);
		}
		
		if( dataType == DataType.json )
		{
			json.add("files", (JsonArray)o);
			out.write(json.toString());
			out.newLine();
		}
		
		if( out != null )
		{
			out.flush();
			out.close();
		}
	}

	private static boolean getBoolean(String v) throws Exception {

		if(
			v.equals("on") ||
			v.equals("enable") ||
			v.equals("true")
		) {
			return true;
		} else if(
			v.equals("off") ||
			v.equals("disable") ||
			v.equals("false")
		) {
			return false;
		} else {
			throw new Exception();
		}
		
	}

	private static int outputList(final HashMap<String, String> hmap, final FileTag tag, final Object out) throws IOException
	{
		int recoads = 0;
		
		Iterator<String> iter = hmap.keySet().iterator();
		
		while( iter.hasNext() )
		{
			String k = iter.next();
			String v = hmap.get(k);
			String s = tag+spliterator+k+spliterator+v;
			
			if( fStdoutData )
			{
				System.out.println(s);	
			}
			
			if( out != null )
			{
				if( out instanceof BufferedWriter )
				{
					BufferedWriter o = (BufferedWriter)out;
					o.write(s);
					o.newLine();
				}
				
				else if( out instanceof JsonArray )
				{
					JsonObject j = new JsonObject();
					j.addProperty("tag", tag.toString());
					j.addProperty("path", k);
					j.addProperty("hash", v);
					
					((JsonArray)out).add(j);
				}
			}
			
			++recoads;
		}
		
		return recoads;
	}


}

