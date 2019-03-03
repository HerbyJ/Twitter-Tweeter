import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


public class Main extends Application{
	
	//Keys to Twitter
	public static String consumerKey = "";
	public static String consumerSecret = "";
	public static String accessToken = "";
	public static String accessSecret = "";
	
	public static ArrayList<Tweet> tweetList = new ArrayList<Tweet>();
	public static TableView tblVw_Tweets;
	public static Timer timer;
	public static int delay = 1000; //Milliseconds
	public static int period = 1000 * 60 * 15; //Milliseconds converted to Minutes (15 minutes)
	//public static Twitter twitter;
	public static int arrayIncrement = 0;
	
	public static void main(String[] args) {
		logIn();
		getTweetInfoFromXML();
		tweetingTimer();
		launch(args);
	}
	
	public static void tweetingTimer(){
		
		//Create the timer.
		timer = new Timer();
		
		//Create the timer task at a scheduled rate.
		timer.scheduleAtFixedRate(new TimerTask(){
			@Override
			public void run(){
				
				//Store the past 15 tweets from the user's timeline in a List<>.
				List<Status> statuses;
				try {
					statuses = GlobalVars.twitter.getUserTimeline();
				
					//Iterate through the list of users, and retweet their status.
					for (Tweet t : tweetList){
						//Iterate through the List<>. If the first 10 chars == the Arraylist<> list's 10 chars, attain the status ID,
						//and remove that tweet from the user's timeline.
						//NOTE: Twitter requires this to be done to avoid spamming.
						for (Status status : statuses){
							//Verify the strings match if List<> statuses and ArrayList<> list
							if(status.getText().substring(0, 10).equals(tweetList.get(arrayIncrement).getSaying().substring(0, 10))){
								
								//Attain the status ID from List<> statuses and remove that status from the user's timeline
								long statusID = status.getId();
								GlobalVars.twitter.destroyStatus(statusID);
							}
						}
					}
					
					//Send out a tweet
					Status postStatus = GlobalVars.twitter.updateStatus(
							tweetList.get(arrayIncrement).getSaying() + "\n" 
							+ tweetList.get(arrayIncrement).getHashtags() + "\n"
							+ tweetList.get(arrayIncrement).getLink());
					
					//Increment the variable used to navigate ArrayList<> list. If its value is > list.size, set it to zero.
					if(arrayIncrement < tweetList.size() - 1){
						arrayIncrement++;
					}else{
						arrayIncrement = 0;
					}
				//Catch and display/log the tweeting error.
				} catch (TwitterException e) {
				}
			}
		}, delay, period); 	
	}
	
	@Override
    public void start(Stage primaryStage) {
        
		//The following criteria sets the title, and builds the TableView tblVw_Tweets.
		primaryStage.setTitle("Herby's Tweeting Machine!");
        
		tblVw_Tweets = new TableView<Tweet>();
        tblVw_Tweets.setMaxSize(1500, 600);
        
        TableColumn<Tweet, String> col1 = new TableColumn<>("ID");
        tblVw_Tweets.getColumns().add(col1);
        col1.setMinWidth(100);
        
        TableColumn<Tweet, String> col2 = new TableColumn<>("Tweet Saying");
        tblVw_Tweets.getColumns().add(col2);
        col2.setMinWidth(500);
        
        TableColumn<Tweet, String> col3 = new TableColumn<>("Hashtags");
        tblVw_Tweets.getColumns().add(col3);
        col3.setMinWidth(500);
        
        TableColumn<Tweet, String> col4 = new TableColumn<>("Link");
        tblVw_Tweets.getColumns().add(col4);
        col4.setMinWidth(500);
        
        //Bind the columns of the table with User values,.
        col1.setCellValueFactory(new PropertyValueFactory<>("id"));
        col2.setCellValueFactory(new PropertyValueFactory<>("saying"));
        col3.setCellValueFactory(new PropertyValueFactory<>("hashtags"));
        col4.setCellValueFactory(new PropertyValueFactory<>("link"));
        
        //Populate the table with the list created from the XML file.
        tblVw_Tweets.setItems(getTweetList());
        
        Label lbl_saying = new Label("Enter your tweet saying.");
        Label lbl_hashtags = new Label("Enter your hashtags");
        Label lbl_link = new Label("Enter your link");
        Label lbl_id = new Label("Enter the ID");
        
        TextField txtFld_saying = new TextField();
        TextField txtFld_hashtags = new TextField();
        TextField txtFld_link = new TextField();
        TextField txtFld_id = new TextField();
        
        txtFld_saying.setMinWidth(700);
        txtFld_hashtags.setMinWidth(700);
        txtFld_link.setMinWidth(700);
        
		Button btn_Add = new Button();
		btn_Add.setText("Add New Tweet");
		btn_Add.setMinWidth(120);
		btn_Add.setOnAction(new EventHandler<ActionEvent>() {
		    @Override
		    public void handle(ActionEvent event) {
		    	Alert alert = new Alert(AlertType.INFORMATION);  
		    	if(txtFld_saying.getText().equals("")){
		        	  alert.setContentText("Please .");
                	  alert.showAndWait();
                	  return;
		        }
		    	
		    	if(txtFld_hashtags.getText().equals("")){
		    		alert.setContentText("Please .");
              	    alert.showAndWait();
              	    return;
		    	}
		    	
		    	if(txtFld_link.getText().equals("")){
		    		alert.setContentText("Please .");
              	    alert.showAndWait();
              	    return;
		    	}
		    	
		    	try {
					DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			        DocumentBuilder documentBuilder;
			        
					documentBuilder = documentBuilderFactory.newDocumentBuilder();
					Document document = documentBuilder.parse(GlobalVars.xmlTweetListLocation);
			        Element root = document.getDocumentElement();

			        Element eTweet = document.createElement("tweet");

			        Element id = document.createElement("id");
			        id.appendChild(document.createTextNode(txtFld_id.getText()));
			        eTweet.appendChild(id);

			        Element saying = document.createElement("saying");
			        saying.appendChild(document.createTextNode(txtFld_saying.getText()));
			        eTweet.appendChild(saying);

			        Element hashtags = document.createElement("hashtags");
			        hashtags.appendChild(document.createTextNode(txtFld_hashtags.getText()));
			        eTweet.appendChild(hashtags);
			        
			        Element link = document.createElement("link");
			        link.appendChild(document.createTextNode(txtFld_link.getText()));
			        eTweet.appendChild(link);
			        
			        root.appendChild(eTweet);

			        DOMSource source = new DOMSource(document);

			        TransformerFactory transformerFactory = TransformerFactory.newInstance();
			        Transformer transformer = transformerFactory.newTransformer();
			        StreamResult result = new StreamResult(GlobalVars.xmlTweetListLocation);
			        transformer.transform(source, result);
				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				} catch (SAXException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (TransformerConfigurationException e) {
					e.printStackTrace();
				} catch (TransformerException e) {
					e.printStackTrace();
				}
		    	
		    	getTweetInfoFromXML();
		    }
		});
        
		Button btn_Remove = new Button();
		btn_Remove.setText("Remove Selected Tweet");
		btn_Remove.setMinWidth(120);
		btn_Remove.setOnAction(new EventHandler<ActionEvent>() {
		    @Override
		    public void handle(ActionEvent event) {
		    	Tweet selectedTweet = (Tweet) tblVw_Tweets.getSelectionModel().getSelectedItem();
            	if(selectedTweet == null){
                	return;
            	}
            	
            	//Remove the user from the XML file and table, and attain the new XML file values and add them to the table.
            	try {
        			//Create the file and params needed to remove the user from the XML file.
        			File fXmlFile = new File(GlobalVars.xmlTweetListLocation);
        			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        			Document doc = dBuilder.parse(fXmlFile);
        			doc.getDocumentElement().normalize();
        			
        			//Find the root node.
        			NodeList mainNode = doc.getElementsByTagName("twitter");
        			
        			//Attain the user nodes.
        			NodeList nList = doc.getElementsByTagName("tweet");
        			
        			//Iterate through the file and find the username to remove.
        			for (int temp = 0; temp < nList.getLength(); temp++) {
        				
        				//Current user node to compare
        				Node nNode = nList.item(temp);
        				
        				//If the node is the right node, continue on
        				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
        					Element eElement = (Element) nNode;
        					
        					//If the node value equals the username value, remove the node and its sub contents.
        					if(eElement.getElementsByTagName("id").item(0).getTextContent().equals(selectedTweet.getID())){
        						//Remove the node
        						mainNode.item(0).removeChild(nNode);
        						doc.getDocumentElement().normalize();
        						
        						//Save the changes to the XML file.
        						DOMSource source = new DOMSource(doc);
        				        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        				        Transformer transformer = transformerFactory.newTransformer();
        				        StreamResult result = new StreamResult(GlobalVars.xmlTweetListLocation);
        				        transformer.transform(source, result);
        					}
        				}
        			}
        		//Catch exceptions but simply ignore them.
        	    } catch (Exception e) {
        	    }
            	getTweetInfoFromXML();
		    }
		});
        
		//Set up the root portion of the GUI and add the GUI params.
		AnchorPane root = new AnchorPane();
		root.getChildren().add(tblVw_Tweets);
		
		root.getChildren().add(lbl_saying);
		root.getChildren().add(lbl_hashtags);
		root.getChildren().add(lbl_link);
		
		root.getChildren().add(txtFld_saying);
		root.getChildren().add(txtFld_hashtags);
		root.getChildren().add(txtFld_link);
		
		root.getChildren().add(btn_Add);
		root.getChildren().add(btn_Remove);
		
		root.getChildren().add(lbl_id);
		root.getChildren().add(txtFld_id);
		
		//TABLE
		root.getChildren().get(0).setLayoutX(10);
        root.getChildren().get(0).setLayoutY(10);
        
        //LABELS
        root.getChildren().get(1).setLayoutX(10);
        root.getChildren().get(1).setLayoutY(440);
        root.getChildren().get(2).setLayoutX(10);
        root.getChildren().get(2).setLayoutY(500);
        root.getChildren().get(3).setLayoutX(10);
        root.getChildren().get(3).setLayoutY(560);
        
        //TEXT FIELDS
        root.getChildren().get(4).setLayoutX(10);
        root.getChildren().get(4).setLayoutY(460);
        root.getChildren().get(5).setLayoutX(10);
        root.getChildren().get(5).setLayoutY(520);
        root.getChildren().get(6).setLayoutX(10);
        root.getChildren().get(6).setLayoutY(580);
        
        //BUTTONS
        root.getChildren().get(7).setLayoutX(590);
        root.getChildren().get(7).setLayoutY(610);
        root.getChildren().get(8).setLayoutX(1350);
        root.getChildren().get(8).setLayoutY(420);
        
        //ADDITIONAL ADDONS
        root.getChildren().get(9).setLayoutX(10);
        root.getChildren().get(9).setLayoutY(620);
        root.getChildren().get(10).setLayoutX(10);
        root.getChildren().get(10).setLayoutY(640);
        
		//Set the primary stage, and show it.
		primaryStage.setScene(new Scene(root, 1500, 690));
		primaryStage.show();
        
        //Ensure the program actual stops all threads when the program is closed.
        //This code stops an issue I was having earlier where the program was closed, but the operation remained.
        Platform.setImplicitExit(true);
        primaryStage.setOnCloseRequest((ae) -> {
            Platform.exit();
            System.exit(0);
        });
    }
	
	public static ObservableList<Tweet> getTweetList(){
		ObservableList<Tweet> tweetsList = FXCollections.observableArrayList();
		
		for(int i = 0; i < tweetList.size(); i++){
			tweetsList.add(tweetList.get(i));
        }
		return tweetsList;
	}
	
	public static void getTweetInfoFromXML(){
		
		boolean isRefreshTable = false;
		if(tweetList.size() > 0){
			for(int i = 0; i < tweetList.size(); i++){
				tweetList.get(i).equals(null);
			}
			
			isRefreshTable = true;
			tweetList.clear();
		}
		
		//Find the XML file, and read the values from it. Find the user's and create TwitterHandleUsers objects from them.
		try {
			//Read the file
			File fXmlFile = new File(GlobalVars.xmlTweetListLocation);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();
			
			//Look at the user nodes and iterate through them.
			NodeList nList = doc.getElementsByTagName("tweet");
			for (int temp = 0; temp < nList.getLength(); temp++) {
				//Instance of the user node.
				Node nNode = nList.item(temp);
				
				//Ensure the nNode is indeed an Element Node
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					
					//Create the TwitterHandleUsers object
					Tweet t = new Tweet();
					
					t.setID(eElement.getElementsByTagName("id").item(0).getTextContent());
					t.setSaying(eElement.getElementsByTagName("saying").item(0).getTextContent());
					t.setHashtags(eElement.getElementsByTagName("hashtags").item(0).getTextContent());
					t.setLink(eElement.getElementsByTagName("link").item(0).getTextContent());
					
					//Add the new object to the tweetList.
					tweetList.add(t);
				}
			}
		//Catch and log any exceptions	
	    } catch (Exception e) {
	    }
		
		if(isRefreshTable){
			refreshTable();
		}
	}
	
	public static void refreshTable(){
		tblVw_Tweets.setItems(getTweetList());
	}
	
	/*******************************************************************************************
	 * Method: logIn()
	 * Purpose: This method will contact the Twitter service, and allow us to login using the credentials
	 * 			stored in the fields section of this class.
	 * Params: N/A
	 * *******************************************************************************************/
	public static void logIn(){
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
		.setOAuthConsumerKey(consumerKey)
		.setOAuthConsumerSecret(consumerSecret)
		.setOAuthAccessToken(accessToken)
		.setOAuthAccessTokenSecret(accessSecret)
		.setTweetModeExtended(true);
		
		TwitterFactory tf = new TwitterFactory(cb.build());
		GlobalVars.twitter = tf.getInstance();
	}
}