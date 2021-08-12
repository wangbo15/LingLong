package edu.pku.sei.conditon.util.repo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import edu.pku.sei.conditon.dedu.DeduMain;

/**
 * Copyright 2017 PLDE, PKU. All right reserved.
 * @author Wang Bo
 * Apr 25, 2017
 */
public class CommitResoter {
	
	private static List<SVNCommit> getSVNCommits(String path){
		
		List<SVNCommit> result = new ArrayList<>();
		
		File file = new File(path);
		try {
			FileReader reader = new FileReader(file);
			BufferedReader br = new BufferedReader(reader);
			
			String line = null;
			while((line = br.readLine()) != null){
				
				if(line.startsWith("------------------------------------------------------------------------")){
					String commitMsg = br.readLine();
					if(commitMsg == null){
						break;
					}
					String[] msges = commitMsg.split("\\|");
					
					long revisionID = new Long(msges[0].trim().substring(1).trim());
					
					String dateStr = msges[2].split("\\+")[0].trim();
					
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
					
					Date date = format.parse(dateStr);
					
					SVNCommit svnCommit = new SVNCommit(revisionID, date);
					
					result.add(svnCommit);
				}
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
		
	}
	
	private static List<SVNBugInfo> parseSvnDB(String subject, String db) {
		
		List<SVNBugInfo> result = new ArrayList<SVNBugInfo>();
		
		File file = new File(db);
		FileReader reader;
		try {
			reader = new FileReader(file);
			BufferedReader br = new BufferedReader(reader);

			String line = null;
			while ((line = br.readLine()) != null) {
				String[] items = line.split("\\,");
				int bug = new Integer(items[0]);
				long diffVersion = new Long(items[1]);
				long repairedVersion = new Long(items[2]);

				SVNBugInfo bugInfo = new SVNBugInfo(subject, bug, repairedVersion, diffVersion);
				
				result.add(bugInfo);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	private static List<GitCommit> getGigCommits(String log) {
		List<GitCommit> result = new ArrayList<>();
		File file = new File(log);
		FileReader reader;
		try {
			reader = new FileReader(file);
			BufferedReader br = new BufferedReader(reader);
			
			String line = null;
			while((line = br.readLine()) != null){
				if(line.startsWith("commit")){
					
					String hash = line.split(" ")[1];
					
					String dateMsg = null;
					
					while((dateMsg = br.readLine()) != null){
						if(dateMsg.startsWith("Date:")){
							dateMsg = dateMsg.substring(5);
							dateMsg = dateMsg.split("\\+")[0].trim();
							
							SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd hh:mm:ss yyyy");
							
							Date date = format.parse(dateMsg);
							
							GitCommit commit = new GitCommit(hash, date);
							
							result.add(commit);
							
							break;
						}
						
					}
				}
			
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
				
		return result;
	}

	private static List<GitBugInfo> parseGitDB(String subject, String db) {
		
		List<GitBugInfo> result = new ArrayList<GitBugInfo>();
		
		File file = new File(db);
		FileReader reader;
		try {
			reader = new FileReader(file);
			BufferedReader br = new BufferedReader(reader);

			String line = null;
			while ((line = br.readLine()) != null) {
				String[] items = line.split("\\,");
				int bug = new Integer(items[0]);
				String diffHash = items[1];
				String repairedHash = items[2];

				GitBugInfo bugInfo = new GitBugInfo(subject, bug, repairedHash, diffHash);
				
				result.add(bugInfo);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return result;
	}

	private static void sortGitBug(String subject, String db, String log) {
		List<GitCommit> commitList = getGigCommits(log);
		List<GitBugInfo> dbList = parseGitDB(subject, db);
		
		for(GitBugInfo bug: dbList){
			boolean found = false;
			
			for(GitCommit commit: commitList){
				if(bug.getRepairedHash().equals(commit.getHashKey())){
					bug.setCommit(commit);
					found = true;
				}
			}
			
			assert found == true;
		}
		
		
		Collections.sort(dbList);
		for(GitBugInfo bug : dbList){
			System.out.println(bug.toString());
		}
	}
	
	private static void sortSvnBug(String subject, String db, String log){
		List<SVNCommit> commitList = getSVNCommits(log);
		
		List<SVNBugInfo> dbList = parseSvnDB(subject, db);
		
		for(SVNBugInfo bug : dbList){
			boolean found = false;
			for(SVNCommit commit : commitList){
				if(bug.getRepairedVersion() == commit.getRevision()){
					bug.setCommit(commit);
					found = true;
					break;
				}
			}
			
			assert found == true;
		}
		
		Collections.sort(dbList);
		
		for(SVNBugInfo bug : dbList){
			System.out.println(bug.toString());
		}
	}
	
	private final static String D4J_PROJ = DeduMain.USER_HOME + "/workspace/defects4j/framework/projects";
	
	public static void main(String[] args){
		
		String chartDB = D4J_PROJ + "/Chart/commit-db";
		String chartLog = DeduMain.USER_HOME + "/workspace/real_repos/jfreechart.log";
		sortSvnBug("Chart", chartDB, chartLog);
		
		System.out.println();
		
		String mathDB = D4J_PROJ + "/Math/commit-db";
		String mathLog = DeduMain.USER_HOME + "/workspace/real_repos/math.log";
		sortGitBug("Math", mathDB, mathLog);
		
		System.out.println();
		
		String timeDB = D4J_PROJ + "/Time/commit-db";
		String timeLog = DeduMain.USER_HOME + "/workspace/real_repos/time.log";
		sortGitBug("Time", timeDB, timeLog);
	
		System.out.println();
		
		String langDB = D4J_PROJ + "/Lang/commit-db";
		String langLog = DeduMain.USER_HOME + "/workspace/real_repos/lang.log";
		sortGitBug("Lang", langDB, langLog);
	}


	
}


class GitCommit implements Comparable<GitCommit>{
	private String hashKey;
	private Date date;
		
	public GitCommit(String hashKey, Date date) {
		super();
		this.hashKey = hashKey;
		this.date = date;
	}
	public String getHashKey() {
		return hashKey;
	}
	public void setHashKey(String hashKey) {
		this.hashKey = hashKey;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	
	@Override
	public String toString() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
		return hashKey + "\t" + sdf.format(date);
	}
	
	@Override
	public int compareTo(GitCommit obj) {
		return this.date.compareTo(obj.getDate());
	}
}


class SVNCommit implements Comparable<SVNCommit>{
	private long revision;
	private Date date;

	public SVNCommit(long revision, Date date) {
		super();
		this.revision = revision;
		this.date = date;
	}
	
	public long getRevision() {
		return revision;
	}
	public void setRevision(long revision) {
		this.revision = revision;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	
	
	
	@Override
	public String toString() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
		return revision + "\t" + sdf.format(date);
	}

	@Override
	public int compareTo(SVNCommit obj) {
		return this.date.compareTo(obj.getDate());
	}
}

class GitBugInfo implements Comparable<GitBugInfo>{
	private String subject;
	private int id;
	private String repairedHash;
	private String diffHash;
	
	private GitCommit commit;

	public GitBugInfo(String subject, int id, String repairedHash, String diffHash) {
		super();
		this.subject = subject;
		this.id = id;
		this.repairedHash = repairedHash;
		this.diffHash = diffHash;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getRepairedHash() {
		return repairedHash;
	}

	public void setRepairedHash(String repairedHash) {
		this.repairedHash = repairedHash;
	}

	public String getDiffHash() {
		return diffHash;
	}

	public void setDiffHash(String diffHash) {
		this.diffHash = diffHash;
	}

	public GitCommit getCommit() {
		return commit;
	}

	public void setCommit(GitCommit commit) {
		this.commit = commit;
	}
	
	@Override
	public int compareTo(GitBugInfo obj) {
		return this.commit.compareTo(obj.getCommit());
	}
	
	@Override
	public String toString() {
		return  subject + "_" + id + ":\t" + commit;
	}
}

class SVNBugInfo implements Comparable<SVNBugInfo>{
	private String subject;
	private int id;
	private long repairedVersion;
	private long diffVersion;
	
	private SVNCommit commit;
	
	public SVNBugInfo(String subject, int id, long repairedVersion, long diffVersion) {
		super();
		this.subject = subject;
		this.id = id;
		this.repairedVersion = repairedVersion;
		this.diffVersion = diffVersion;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public long getRepairedVersion() {
		return repairedVersion;
	}
	public void setRepairedVersion(long repairedVersion) {
		this.repairedVersion = repairedVersion;
	}
	public long getDiffVersion() {
		return diffVersion;
	}
	public void setDiffVersion(long diffVersion) {
		this.diffVersion = diffVersion;
	}
	public SVNCommit getCommit() {
		return commit;
	}
	public void setCommit(SVNCommit commit) {
		this.commit = commit;
	}
	@Override
	public int compareTo(SVNBugInfo obj) {
		return this.commit.compareTo(obj.getCommit());
	}
	@Override
	public String toString() {
		return  subject + "_" + id + ":\t" + commit;
	}

}
