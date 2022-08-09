package com.todo.doBlog.controller;

import java.lang.reflect.Array;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.springframework.format.datetime.DateFormatter;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.HtmlUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.todo.doBlog.objects.BlogObject;
import com.todo.doBlog.objects.SimpleObject;

@RestController
@Controller
public class DoBlogController {
	 
	 public Map<String,Integer> wordCountMap;
	 public int blogCount;
	 public Date maxDate;
	 public List<BlogObject> blogObjects;
	 
	 public DoBlogController() {
		wordCountMap = new HashMap<>();
		maxDate = null;
		blogObjects = null;
	}
	 
	 public JsonNode fetchJsonNode( String uri ) throws Exception {
		 RestTemplate restTemplate = new RestTemplate();
		 ResponseEntity<String> response = (ResponseEntity<String>)restTemplate.getForEntity(uri , String.class);
		 
		 ObjectMapper mapper = new ObjectMapper();
		 
		 return mapper.readTree(response.getBody());
	 }
	 
	 @MessageMapping("/initData")
	 @SendTo("/topic/receiveData")
	 public SimpleObject initData() {
		 		 
		blogObjects = new ArrayList();
		 
		try {
			JsonNode root = fetchJsonNode("https://www.internate.org/wp-json/wp/v2/posts/?context=embed");
			Iterator<JsonNode> nodes = root.elements();
			while(nodes.hasNext()) {
				JsonNode element = nodes.next();
				BlogObject newBlogObject = buildBlogObject( element );
				blogObjects.add(newBlogObject);
				
				Date blogDate = newBlogObject.getDate();
				if( maxDate == null || (blogDate != null && maxDate.before(blogDate) ) ) {
					maxDate = blogDate;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new SimpleObject("error", "Fehler beim Laden der URL!", Integer.valueOf(1));
		}
		
		wordCountMap = new HashMap<>();
		for( BlogObject blogObject : blogObjects ) {
			updateWordCountMap( removeWhitespace( blogObject.getContent() ) );
		}
		 
		 return new SimpleObject("succes", "Daten geladen!", Integer.valueOf(1));
	 }

	 @MessageMapping("/getData")
	 @SendTo("/topic/showData")
	 public List<BlogObject> sendBlogObjects() {
		 		 
		 return blogObjects;
	 }
	 
	 @MessageMapping("/update")
	 @SendTo("/topic/updateData")
	 public SimpleObject updateBlogs() {
		 
		 boolean needUpdate = false;
		 
		try {
			JsonNode root = fetchJsonNode("https://www.internate.org/wp-json/wp/v2/posts/?context=embed");
			
			Iterator<JsonNode> nodes = root.elements();
			while(nodes.hasNext()) {
				JsonNode element = nodes.next();
				String dateString = element.get("date").toString();
				dateString = removeWhitespace(dateString);
				
				LocalDateTime dateTime = LocalDateTime.parse(dateString);
				Instant instant = dateTime.atZone(ZoneId.systemDefault()).toInstant();
				Date blogDate = Date.from(instant);	
				
				if( maxDate == null || (blogDate != null && maxDate.before(blogDate) ) ) {
					maxDate = blogDate;
					needUpdate = true;
				}
			}
			
		} catch (Exception e) {
			 return new SimpleObject("error", "Updateerror");
		}
		 
		if( needUpdate ) {
			return new SimpleObject("updated", "Update");			
		}
		
		return new SimpleObject("notUpdated", "No Update needed");
	 }
	 
	 
	private BlogObject buildBlogObject(JsonNode element) {
	
		
		String id = element.get("id").toString();
		 String uri = "https://www.internate.org/wp-json/wp/v2/posts/" + id;
		 RestTemplate restTemplate = new RestTemplate();
		 ResponseEntity<String> response = (ResponseEntity<String>)restTemplate.getForEntity(uri , String.class);
		 
		 Date date = null;
		 String content = null;
		 String link = null;
		 String title = null;
		 
		 try {
			 JsonNode contentElement = element.get("excerpt");
			 content = contentElement.get("rendered").toString();
			 content = content.replace("\"", "");
			 content = removeWhitespace(content);

			 ObjectMapper mapper = new ObjectMapper();
			 JsonNode root = mapper.readTree(response.getBody());
				
				link = element.get("link").toString();
				link = link.replace("\"", "");
				
				JsonNode titleElement = root.get("title");
				title = titleElement.get("rendered").toString();
				title = title.replace("\"", "");
				content = content.replace(title, "");
				
				String dateString = root.get("modified").toString();
				if( dateString == null ) {
					root.get("date").toString();
				}
				dateString = removeWhitespace(dateString);
				
				LocalDateTime dateTime = LocalDateTime.parse(dateString);
				Instant instant = dateTime.atZone(ZoneId.systemDefault()).toInstant();
				date = Date.from(instant);	
			
		} catch (Exception e) {
//			return new SimpleObject(id, "Fehler beim Laden der URL!", Integer.valueOf(1));
		}
		
		return new BlogObject(id, date, content, link, title );
		
	}
	
	public void updateWordCountMap( String content ) {
		
		if( content == null || content.isEmpty() ) {
			return;
		}
		
		int curIndex = 0;
		int nextIndex = 0;
		
		while( curIndex < content.length() ) {
			
			nextIndex = content.indexOf(" ", curIndex );
			String word = content.substring(curIndex, nextIndex);
			word = word.replace(",", "");
			word = word.replace(".", "");
			
			if( wordCountMap.containsKey(word) ) {
				Integer cnt = wordCountMap.get(word);
				int cntInt = cnt.intValue();
				cntInt++;
				
				wordCountMap.put(word, Integer.valueOf(cntInt) );
			} else {

				wordCountMap.put(word, Integer.valueOf(1) );
			}
			
			curIndex = ++nextIndex;
		}		
	}
	
	public String removeWhitespace( String toRemove ) {
		if( toRemove == null || toRemove.isEmpty() ) {
			return toRemove;
		}
		
		toRemove = toRemove.replace("\"", "");
		toRemove = toRemove.replace("\\n", "");
		toRemove = toRemove.replace("[&hellip;]", "");
		
		return toRemove.replaceAll("\\<.*?>", "");
		
	}
}
