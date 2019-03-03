
public class Tweet {

	private String id;
	private String saying;
	private String hashtags;
	private String link;
	
	public String getID(){
		return id;
	}
	
	public String getSaying(){
		return saying;
	}
	
	public String getHashtags(){
		return hashtags;
	}
	
	public String getLink(){
		return link;
	}
	
	public void setID(String i){
		id = i;
	}
	
	public void setSaying(String s){
		saying = s;
	}
	
	public void setHashtags(String h){
		hashtags = h;
	}
	
	public void setLink(String l){
		link = l;
	}
}
