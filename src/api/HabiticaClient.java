package api;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.Timer;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class HabiticaClient {

	private JSONParser parser = new JSONParser();
	private String baseURL = "https://habitica.com/api/v3/";
	private String api_token = "5082f9c4-7c04-4981-8096-1a3ee24b22e9";
	private String user_id = "b58f5fda-39a8-4de0-9184-7225433dc325";
	private JSONArray todos = new JSONArray();
	private JSONArray dailies = new JSONArray();
	private JSONArray habits = new JSONArray();
	private double experience = 0;
	private int level = 0;
	private double hp = 0;
	private double mp = 0;
	private int toNextLevel = 0;
	private String sampleTask = new String("{\"text\":\"Lire\",\"type\":\"habit\",\"value\":0,\"tags\":[],\"notes\":\"Created by habitica desktop client\"}");
	
	public HabiticaClient() {
		readCredentials();

	}

	public void readCredentials() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader("credentials.txt"));
			this.api_token = reader.readLine();
			this.user_id = reader.readLine();
		} catch (FileNotFoundException e) {
			System.out.println("Please create file \'credentials.txt\' and add it your credentials.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void requestTasks() {
		String urlString = baseURL + "tasks/user";
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(urlString);
		request.addHeader("x-api-key", api_token);
		request.addHeader("x-api-user", user_id);

		try {
			HttpResponse response = client.execute(request);

			BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

			StringBuffer result = new StringBuffer();
			String line;
			while((line = reader.readLine()) != null) {
				result.append(line);
			}

			JSONObject obj = (JSONObject)parser.parse(result.toString());
			JSONArray data = (JSONArray)obj.get("data");
			System.out.println(data);
			for(Object o : data) {
				JSONObject o2 = (JSONObject)o;
				if(o2.get("type").equals("habit")) {
					habits.add(o2);
				} else if(o2.get("type").equals("todo")) {
					todos.add(o2);
				} else if(o2.get("type").equals("daily")) {
					dailies.add(o2);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}

	}

	public void getUserInfo() {
		//Request building
		String urlString = baseURL + "user";
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(urlString);
		request.addHeader("x-api-key", api_token);
		request.addHeader("x-api-user", user_id);

		try {
			//Gets the response from the server
			HttpResponse response = client.execute(request);
			BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

			//Converts the response to a String
			StringBuffer result = new StringBuffer();
			String line;
			while((line = reader.readLine()) != null) {
				result.append(line);
			}

			Object obj = parser.parse(result.toString());
			JSONObject answer = (JSONObject)((JSONObject)((JSONObject)obj).get("data")).get("stats");
			this.experience = Double.parseDouble(answer.get("exp").toString());
			this.hp = Double.parseDouble(answer.get("hp").toString());
			this.mp = Double.parseDouble(answer.get("mp").toString());
			this.toNextLevel = Integer.parseInt(answer.get("toNextLevel").toString());
			this.level = Integer.parseInt(answer.get("lvl").toString());


		} catch(IOException | ParseException e) {
			e.printStackTrace();
		}
	}
	
	public void upgradeTask(String taskID, String direction) {
		if(direction.equals("up") || direction.equals("down")) {
			String url = baseURL + "tasks/" + taskID + "/score/" + direction;HttpClient client = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(url);
			request.addHeader("x-api-key", api_token);
			request.addHeader("x-api-user", user_id);


			try {
				HttpResponse response = client.execute(request);
				int responseCode = response.getStatusLine().getStatusCode();

				if(responseCode == 200) {
					System.out.println("Success");
				} else {
					System.err.println("Error : response code : " + responseCode);
				}

			} catch(IOException e) {
				e.printStackTrace();
			}

		} else {
			return;
		}


	}
	
	public void createTask(String title, String type, String notes) throws FileNotFoundException {
			
		try {
			JSONObject obj = (JSONObject)parser.parse(sampleTask);
			obj.put("text", title);
			if(type.equals("habit") || type.equals("todo") || type.equals("daily")) {
				obj.put("type", type);
			} else {
				System.err.println("Error : Wrong type of habit");
				return;
			}
			
			obj.put("notes", notes);
			
			System.out.println(obj.toJSONString());
			
			HttpClient client = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost("https://habitica.com/api/v3/tasks/user");
			request.addHeader("x-api-key", api_token);
			request.addHeader("x-api-user", user_id);
			request.addHeader("Content-Type", "application/json");
			
			StringEntity requestEntity = new StringEntity(obj.toJSONString());
			
			request.setEntity(requestEntity);
			
			HttpResponse response = client.execute(request);
			
			if(response.getStatusLine().getStatusCode() >= 200 && response.getStatusLine().getStatusCode() <= 210) {
				System.out.println("Task created successfully");
			} else {
				System.out.println("Request failed. Http Status code : " + response.getStatusLine().getStatusCode());
				String buf = new String();
				while((buf = new BufferedReader(new InputStreamReader(response.getEntity().getContent())).readLine()) != null) {
					System.out.println(buf);
				}
			}
			
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}

	}

	public JSONArray getTodos() {
		return todos;
	}

	public void setTasks(JSONArray todos) {
		this.todos = todos;
	}

	public JSONArray getDailies() {
		return dailies;
	}

	public void setDailies(JSONArray dailies) {
		this.dailies = dailies;
	}

	public JSONArray getHabits() {
		return habits;
	}

	public void setHabits(JSONArray habits) {
		this.habits = habits;
	}

	public double getExperience() {
		return experience;
	}

	public void setExperience(long experience) {
		this.experience = experience;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public double getHp() {
		return hp;
	}

	public void setHp(long hp) {
		this.hp = hp;
	}

	public double getMp() {
		return mp;
	}

	public void setMp(long mp) {
		this.mp = mp;
	}

	public int getToNextLevel() {
		return toNextLevel;
	}

	public void setToNextLevel(int toNextLevel) {
		this.toNextLevel = toNextLevel;
	}

}
