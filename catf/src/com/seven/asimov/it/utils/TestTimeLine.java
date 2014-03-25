package com.seven.asimov.it.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for storing requests/responses time information.
 * Logging these parameters may help investigating problems.
 * @author msvintsov
 *
 */
public class TestTimeLine {
	
	private static final String TYPE_REQ = "Req";
	private static final String TYPE_RESP = "Resp"; 
	
	private List<TestTimeLineEntry> entries = new ArrayList<TestTimeLineEntry>();
 
	
	public synchronized void request(String id) {
		entries.add(new TestTimeLineEntry(System.currentTimeMillis(), id, TYPE_REQ));
 	}
	 
	 public void request(int id) {
		 request(""+id);
	 }
	 
	 public synchronized void response(String id) {
		 entries.add(new TestTimeLineEntry(System.currentTimeMillis(), id, TYPE_RESP));
	 }
	 
	 public void response(int id) {
		 response(""+id);
	 }
	 
	 public synchronized String getRequests() {
		 long startTime = entries.size() > 0 ? entries.get(0).time : -1;
		 StringBuilder sb = new StringBuilder("Requests: ");
		 long prevTime = -1;
		 for (TestTimeLineEntry entry : entries) {
			 if (TYPE_REQ != entry.type) continue;
			 if (prevTime > -1) {
				 sb.append(" - ");
				 sb.append((entry.time - prevTime)/1000);
				 sb.append("s - ");
			 }
			 long fromStartTime = (entry.time - startTime) / 1000;
			 sb.append("#" + entry.id + "(" +fromStartTime + "s)");
			 prevTime = entry.time;
		 }
		 return sb.toString();
	 }
	 
	 public synchronized String getResponses() {
		 long startTime = entries.size() > 0 ? entries.get(0).time : -1;
		 StringBuilder sb = new StringBuilder("Responses: ");
		 long prevTime = -1;
		 for (TestTimeLineEntry entry : entries) {
			 if (TYPE_RESP != entry.type) continue;
			 if (prevTime > -1) {
				 sb.append(" - ");
				 sb.append((entry.time - prevTime)/1000);
				 sb.append("s - ");
			 }
			 long fromStartTime = (entry.time - startTime) / 1000;
			 sb.append("#" + entry.id + "(" +fromStartTime + "s)");
			 prevTime = entry.time;
		 }
		 return sb.toString();
	 }
	 
	 public synchronized String getDelays() {
		 StringBuilder sb = new StringBuilder("Delays: ");
		 for (TestTimeLineEntry entry : entries) {
			 if (TYPE_REQ.equals(entry.type)) {
				 long reqTime = entry.time;
				 long respTime = 0;
				 TestTimeLineEntry respEntry = getEntry(entry.id, TYPE_RESP);
				 if (respEntry != null) respTime = respEntry.time;
				 sb.append("#" + entry.id + ": ");
				 if (respTime != 0) {
					 sb.append((respTime - reqTime)/1000 + "s;   ");
				 } else {
					 sb.append("?;   ");
				 }
			 }
				 
		 }
		 return sb.toString();
	 }
	 
	 public synchronized String getAll() {
		 StringBuilder sb = new StringBuilder("Req/Resp: ");
		 long prevTime = -1;
		 for (TestTimeLineEntry entry : entries) {
			 if (prevTime > -1) {
				 sb.append(" - ");
				 sb.append((entry.time - prevTime)/1000);
				 sb.append("s - ");
			 }
			 sb.append(entry.type + "#" + entry.id);
			 prevTime = entry.time;
		 }
		 return sb.toString();
	 }

	 private TestTimeLineEntry getEntry(String id, String type) {
		 for (TestTimeLineEntry entry : entries) {
			if (id.equals(entry.id) && type.equals(entry.type)) return entry;
		}
		 return null;
	 }
	
}

class TestTimeLineEntry {
	
	TestTimeLineEntry(long time, String id, String type) {
		this.time = time;
		this.id = id;
		this.type = type;
	}
	
	long time;
	String id;
	String type;
	
}
