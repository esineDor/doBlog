package com.todo.doBlog.objects;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlogObject implements Comparable<BlogObject> {

	protected String id;
	protected Date date;
	protected String content;
	protected String link;
	protected String blogtitle;
	protected Map<String, Integer> wordmap;
	protected List<Object> wordMapList;
	
	public String getId() {
		return id;
	}
	public Date getDate() {
		return date;
	}
	public String getContent() {
		return content;
	}
	public String getLink() {
		return link;
	}
	public String getBlogtitle() {
		return blogtitle;
	}
	public Map<String, Integer> getWordmap() {
		return wordmap;
	}
	
	public List<Object> getWordMapList() {
		return wordMapList;
	}
	
	public BlogObject(String id, Date date, String content, String link, String blogtitle ) {
		super();
		this.id = id;
		this.date = date;
		this.content = content;
		this.link = link;
		this.blogtitle = blogtitle;
		
		buildWordMap( content );
	}
	
	public BlogObject(String id, Date date, String content, String link ) {
		super();
		this.id = id;
		this.date = date;
		this.content = content;
		this.link = link;
		
		buildWordMap( content );
	}
	
	public BlogObject(String id, Date date, String content) {
		super();
		this.id = id;
		this.date = date;
		this.content = content;
		
		buildWordMap( content );
	}
	
	protected void buildWordMap( String content ) {
		
		if( content == null || content.isEmpty() ) {
			return;
		}
		
		Map<String, Integer> newMap = new HashMap<String, Integer>();
		int curIndex = 0;
		int nextIndex = 0;
		
		while( curIndex < content.length() ) {
			
			nextIndex = content.indexOf(" ", curIndex );
			String word = content.substring(curIndex, nextIndex);
			word = word.replace(",", "");
			word = word.replace(".", "");
			
			if( newMap.containsKey(word) ) {
				Integer cnt = newMap.get(word);
				int cntInt = cnt.intValue();
				cntInt++;
				
				newMap.put(word, Integer.valueOf(cntInt) );
			} else {

				newMap.put(word, Integer.valueOf(1) );
			}
			
			curIndex = ++nextIndex;
		}
		
		wordmap = newMap;
		wordMapList = new ArrayList(wordmap.entrySet());
	}
	
	
	@Override
	public int compareTo( BlogObject b ) {
		
		Date date = getDate();
		Date compareDate = b.getDate();
		
		if( date.after(compareDate) ) {
			return -1;
		} else if ( date.before(compareDate) ) {
			return 1;
		}
		
		return 0;
	}
	
}
