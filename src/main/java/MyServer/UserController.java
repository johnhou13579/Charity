package MyServer;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCrypt;

import javax.servlet.http.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.Map;
import java.util.HashMap;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.net.RequestOptions;

import org.json.JSONObject;
import org.json.JSONArray;

import java.io.*;
import java.net.*;

@RestController
public class UserController {

	@RequestMapping(value = "/payment", method = RequestMethod.POST) 
	public ResponseEntity<String> stripeTokenHandler(@RequestBody String payload, HttpServletRequest request) {
		HttpHeaders responseHeaders = new HttpHeaders(); 
		JSONObject payloadObj = new JSONObject(payload);
    	responseHeaders.set("Content-Type", "application/json");

		Stripe.apiKey = "";
		try{
			// Token is created using Stripe Checkout or Elements!
			// Get the payment token ID submitted by the form:
			String token = payloadObj.getString("token");

			Map<String, Object> params = new HashMap<>();
			params.put("amount", 6969);
			params.put("currency", "usd");
			params.put("description", "Example charge");
			params.put("source", token);
			Charge charge = Charge.create(params);

		}catch (StripeException e) {
			// The card has been declined
		}
		
		return new ResponseEntity("{\"message\":\"API Payment Called\"}", responseHeaders, HttpStatus.OK);

	}

	@RequestMapping(value = "/chat", method = RequestMethod.POST) 
	public ResponseEntity<String> chat(@RequestBody String payload, HttpServletRequest request) {
		JSONObject payloadObj = new JSONObject(payload);
		String chat = payloadObj.getString("chat"); 
		String username = payloadObj.getString("username");

		HttpHeaders responseHeaders = new HttpHeaders(); 
    	responseHeaders.set("Content-Type", "application/json");

    	String auth = payloadObj.getString("auth");
		if(!auth.equals(MyServer.users.get(username)))
		{
			return new ResponseEntity("{\"message\":\"Log Out\"}", responseHeaders, HttpStatus.BAD_REQUEST);
		}

    	String hostName = "127.0.0.1";
    	int portNumber = Integer.parseInt("4040");
        try (
            Socket echoSocket = new Socket(hostName, portNumber); //Connect socket to given host name and port number
            PrintWriter out =
                new PrintWriter(echoSocket.getOutputStream(), true); //Get output stream of socket pass it into a PrintWriter so we can write to the server
        ) {
        	JSONObject obj = new JSONObject();

            String userInput = chat;
            ServerListener serverListener = new ServerListener(echoSocket); //Initialize a thread to listen for input from server
            serverListener.start();
            out.println(userInput);

        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                hostName);
            System.exit(1);
        } 


		return new ResponseEntity("{\"message\":\"User Chat Successfully\"}", responseHeaders, HttpStatus.OK);
	}

	@RequestMapping(value = "/register", method = RequestMethod.POST) 
	public ResponseEntity<String> register(@RequestBody String payload, HttpServletRequest request) {
		JSONObject payloadObj = new JSONObject(payload);
		String username = payloadObj.getString("username"); 
		String password = payloadObj.getString("password");

		HttpHeaders responseHeaders = new HttpHeaders(); 
    	responseHeaders.set("Content-Type", "application/json");
		Connection conn = null;

		MessageDigest digest = null;
		String hashedKey = null;
		
		try {
			digest = MessageDigest.getInstance("SHA-256"); 
			hashedKey = bytesToHex(digest.digest(password.getBytes("UTF-8"))); 
		}catch(Exception e) {
		}
		 try {
	    	conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/charity?useUnicode=true&characterEncoding=UTF-8", "root", "snowleopard");
			String query = "INSERT INTO charity.users(users.username,users.hashedPassword) VALUES(?,?)";
			PreparedStatement stmt = null;
	        stmt = conn.prepareStatement(query);
	        stmt.setString(1, username);
	        stmt.setString(2, hashedKey);
	        stmt.executeUpdate();
	    } catch (Exception e) {
	    	return new ResponseEntity("{\"message\":\"User Registered Unsuccessfully\"}", responseHeaders, HttpStatus.BAD_REQUEST);
	    } finally {
	    	try {
	    		if (conn != null) { conn.close(); }
	    	}catch(Exception e) {
	    		return new ResponseEntity("{\"message\":\"User Registered Unsuccessfully\"}", responseHeaders, HttpStatus.OK);
	    	}
	    }

	    if (!MyServer.users.containsKey(username)) {
			//MyServer.users.put(username, hashedKey.substring(0,10));
			
		}else {
			return new ResponseEntity("{\"message\":\"User Registered Unsuccessfully\"}", responseHeaders, HttpStatus.BAD_REQUEST);
		}
		JSONObject responseObj = new JSONObject();
		responseObj.put("message", MyServer.users.get(username));
		return new ResponseEntity(responseObj.toString(), responseHeaders, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/registerCharity", method = RequestMethod.POST) 
	public ResponseEntity<String> registerCharity(@RequestBody String payload, HttpServletRequest request) {
		JSONObject payloadObj = new JSONObject(payload);
		String charityName = payloadObj.getString("charityName");
		String username = payloadObj.getString("username"); 
		String password = payloadObj.getString("password");

		HttpHeaders responseHeaders = new HttpHeaders(); 
    	responseHeaders.set("Content-Type", "application/json");
		Connection conn = null;

		MessageDigest digest = null;
		String hashedKey = null;
		
		try {
			digest = MessageDigest.getInstance("SHA-256"); 
			hashedKey = bytesToHex(digest.digest(password.getBytes("UTF-8"))); 
		}catch(Exception e) {
		}
		 try {
	    	conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/charity?useUnicode=true&characterEncoding=UTF-8", "root", "snowleopard");
			String query = "INSERT INTO charity.charities VALUES(?,0,0.0,?,?)";
			PreparedStatement stmt = null;
	        stmt = conn.prepareStatement(query);
	        stmt.setString(1, charityName);
	        stmt.setString(2, username);
	        stmt.setString(3, hashedKey);
	        stmt.executeUpdate();
	    } catch (Exception e) {
	    	return new ResponseEntity("{\"message\":\"User Registered Unsuccessfully\"}", responseHeaders, HttpStatus.BAD_REQUEST);
	    } finally {
	    	try {
	    		if (conn != null) { conn.close(); }
	    	}catch(Exception e) {
	    		return new ResponseEntity("{\"message\":\"User Registered Unsuccessfully\"}", responseHeaders, HttpStatus.OK);
	    	}
	    }
	    if (!MyServer.users.containsKey(username)) {
			//MyServer.users.put(username, hashedKey.substring(0,10));
			
		}else {
			return new ResponseEntity("{\"message\":\"User Registered Unsuccessfully\"}", responseHeaders, HttpStatus.BAD_REQUEST);
		}
		JSONObject responseObj = new JSONObject();
		responseObj.put("message", MyServer.users.get(username));
		return new ResponseEntity(responseObj.toString(), responseHeaders, HttpStatus.OK);
	}

	@RequestMapping(value = "/login", method = RequestMethod.POST) 
	public ResponseEntity<String> login(@RequestBody String payload, HttpServletRequest request) {
		JSONObject payloadObj = new JSONObject(payload);
		String username = payloadObj.getString("username"); 
		String password = payloadObj.getString("password");

		HttpHeaders responseHeaders = new HttpHeaders(); 
    	responseHeaders.set("Content-Type", "application/json");
		Connection conn = null;

		MessageDigest digest = null;
		String hashedKey = null;
		String hashed = null;

		try {
			digest = MessageDigest.getInstance("SHA-256"); 
			hashedKey = bytesToHex(digest.digest(password.getBytes("UTF-8"))); 
		}catch(Exception e) {
		}
		try {
	    	conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/charity?useUnicode=true&characterEncoding=UTF-8", "root", "snowleopard");
			String query = "SELECT users.username, users.hashedPassword FROM charity.users WHERE username = ?";
			PreparedStatement stmt = null;
	        stmt = conn.prepareStatement(query);
	        stmt.setString(1, username);
	        ResultSet rs = stmt.executeQuery();

	        while (rs.next()) {
	            String user = rs.getString("username");
	            hashed = rs.getString("hashedPassword");
	        }
	    } catch (SQLException e ) {
	    	JSONObject responseObj = new JSONObject();
			responseObj.put("message", "cannot register. try again.");
	    	return new ResponseEntity(e, responseHeaders, HttpStatus.BAD_REQUEST);
	    } finally {
	    	try {
	    		if (conn != null) { conn.close(); }
	    	}catch(SQLException se) {
	    	}
	    }
	    if (hashed.equals(hashedKey)) {
	    		MyServer.users.put(username, hashedKey.substring(0,10));
	    		JSONObject responseObj = new JSONObject();

				responseObj.put("Message", MyServer.users.get(username));
				return new ResponseEntity(responseObj.toString(), responseHeaders, HttpStatus.OK);

			}else {
				return new ResponseEntity("{\"Message\":\"username/password combination is incorrect\"}", responseHeaders, HttpStatus.BAD_REQUEST);
			}
	}

	@RequestMapping(value = "/loginCharity", method = RequestMethod.POST) 
	public ResponseEntity<String> loginCharity(@RequestBody String payload, HttpServletRequest request) {
		JSONObject payloadObj = new JSONObject(payload);
		String charityName = payloadObj.getString("charityName");
		String username = payloadObj.getString("username"); 
		String password = payloadObj.getString("password");

		HttpHeaders responseHeaders = new HttpHeaders(); 
    	responseHeaders.set("Content-Type", "application/json");
		Connection conn = null;

		MessageDigest digest = null;
		String hashedKey = null;
		String hashed = null;

		try {
			digest = MessageDigest.getInstance("SHA-256"); 
			hashedKey = bytesToHex(digest.digest(password.getBytes("UTF-8"))); 
		}catch(Exception e) {
		}
		try {
	    	conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/charity?useUnicode=true&characterEncoding=UTF-8", "root", "snowleopard");
			String query = "SELECT charities.password FROM charity.charities WHERE charities.username = ? AND charities.charity = ?";
			PreparedStatement stmt = null;
	        stmt = conn.prepareStatement(query);
	        stmt.setString(1, username);
	        stmt.setString(2, charityName);
	        ResultSet rs = stmt.executeQuery();

	        while (rs.next()) {
	            hashed = rs.getString("password");
	        }
	    } catch (SQLException e ) {
	    	JSONObject responseObj = new JSONObject();
			responseObj.put("message", "cannot register. try again.");
	    	return new ResponseEntity(e, responseHeaders, HttpStatus.BAD_REQUEST);
	    } finally {
	    	try {
	    		if (conn != null) { conn.close(); }
	    	}catch(SQLException se) {
	    	}
	    }
	    if (hashed.equals(hashedKey)) {
	    		MyServer.users.put(username, hashedKey.substring(0,10));
	    		JSONObject responseObj = new JSONObject();
				responseObj.put("Message", MyServer.users.get(username));

				return new ResponseEntity(responseObj.toString(), responseHeaders, HttpStatus.OK);

			}else {
				return new ResponseEntity("{\"message\":\"username/password combination is incorrect\"}", responseHeaders, HttpStatus.BAD_REQUEST);
			}
	}

	@RequestMapping(value = "/connectToBank", method = RequestMethod.POST) // <-- setup the endpoint URL at /hello with the HTTP POST method
	public ResponseEntity<String> connectToBank(@RequestBody String payload, HttpServletRequest request) {
		JSONObject payloadObj = new JSONObject(payload);
		String username = payloadObj.getString("username"); 
		double balance = Double.parseDouble(payloadObj.getString("balance"));
		String account = payloadObj.getString("bankAccount");

		HttpHeaders responseHeaders = new HttpHeaders(); 
    	responseHeaders.set("Content-Type", "application/json");
		Connection conn = null;
		MessageDigest digest = null;
		String hashedKey = null;

		String auth = payloadObj.getString("auth");
		if(!auth.equals(MyServer.users.get(username)))
		{
			return new ResponseEntity("{\"message\":\"Log Out\"}", responseHeaders, HttpStatus.BAD_REQUEST);
		}


		try {
			digest = MessageDigest.getInstance("SHA-256"); 
			hashedKey = bytesToHex(digest.digest(account.getBytes("UTF-8"))); 
		}catch(Exception e) {
		}

	    try {
	    	conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/charity?useUnicode=true&characterEncoding=UTF-8", "root", "snowleopard");
			String query = "UPDATE charity.users SET balance = ?, hashedBankAccount = ? WHERE (username = ?)";
			PreparedStatement stmt = null;
	        stmt = conn.prepareStatement(query);
	        stmt.setDouble(1, balance);
	        stmt.setString(2, hashedKey);
	        stmt.setString(3, username);

	        stmt.executeUpdate();
	        
	    } catch (Exception e ) {
	    	return new ResponseEntity("{\"message\":\"Error. Try Again\"}", responseHeaders, HttpStatus.BAD_REQUEST);
	    } finally {
	    	try {
	    		if (conn != null) { conn.close(); }
	    	}catch(SQLException se) {

	    	}
	        
	    }
		return new ResponseEntity(balance, responseHeaders, HttpStatus.OK);
	}

	@RequestMapping(value = "/updateBalance", method = RequestMethod.POST) // <-- setup the endpoint URL at /hello with the HTTP POST method
	public ResponseEntity<String> updateBalance(@RequestBody String payload, HttpServletRequest request) {
		JSONObject payloadObj = new JSONObject(payload);
		String username = payloadObj.getString("username"); 

		HttpHeaders responseHeaders = new HttpHeaders(); 
    	responseHeaders.set("Content-Type", "application/json");
		Connection conn = null;
		JSONArray usersArray = new JSONArray();

		String auth = payloadObj.getString("auth");
		if(!auth.equals(MyServer.users.get(username)))
		{
			return new ResponseEntity("{\"message\":\"Log Out\"}", responseHeaders, HttpStatus.BAD_REQUEST);
		}

	    try {
	    	conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/charity?useUnicode=true&characterEncoding=UTF-8", "root", "snowleopard");
			String query = "SELECT users.balance FROM charity.users WHERE username = ?";
			PreparedStatement stmt = null;
	        stmt = conn.prepareStatement(query);
	        stmt.setString(1, username);

	        ResultSet rs = stmt.executeQuery();
			
			while (rs.next()) {
	            Double balance = rs.getDouble("users.balance");

	            JSONObject obj = new JSONObject();
	            obj.put("Balance", balance);
	            usersArray.put(obj);
			   }
	        
	    } catch (Exception e ) {
	    	return new ResponseEntity("{\"message\":\"Error. Try Again\"}", responseHeaders, HttpStatus.BAD_REQUEST);
	    } finally {
	    	try {
	    		if (conn != null) { conn.close(); }
	    	}catch(SQLException se) {

	    	}
	        
	    }
		return new ResponseEntity(usersArray.toString(), responseHeaders, HttpStatus.OK);
	}

	@RequestMapping(value = "/updateBalanceCharity", method = RequestMethod.POST) // <-- setup the endpoint URL at /hello with the HTTP POST method
	public ResponseEntity<String> updateBalanceCharity(@RequestBody String payload, HttpServletRequest request) {
		JSONObject payloadObj = new JSONObject(payload);
		String username = payloadObj.getString("username"); 

		HttpHeaders responseHeaders = new HttpHeaders(); 
    	responseHeaders.set("Content-Type", "application/json");
		Connection conn = null;
		JSONArray usersArray = new JSONArray();

		String auth = payloadObj.getString("auth");
		if(!auth.equals(MyServer.users.get(username)))
		{
			return new ResponseEntity("{\"message\":\"Log Out\"}", responseHeaders, HttpStatus.BAD_REQUEST);
		}

	    try {
	    	conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/charity?useUnicode=true&characterEncoding=UTF-8", "root", "snowleopard");
			String query = "SELECT * FROM charity.transactions, charity.charities WHERE transactions.charity = charities.charity AND charities.username = ?";
			PreparedStatement stmt = null;
	        stmt = conn.prepareStatement(query);
	        stmt.setString(1, username);

	        ResultSet rs = stmt.executeQuery();
			
			   while (rs.next()) {
	            Double balance = rs.getDouble("charities.donatesTotal");
	            String user = rs.getString("transactions.username");
	            Double donatedAmount = rs.getDouble("transactions.donatedAmount");
	            JSONObject obj = new JSONObject();
	            obj.put("Balance", balance);
	            obj.put("User", user);
	            obj.put("Donated", donatedAmount);
	            usersArray.put(obj);
			   }
	        
	    } catch (Exception e ) {
	    	return new ResponseEntity(e, responseHeaders, HttpStatus.BAD_REQUEST);
	    } finally {
	    	try {
	    		if (conn != null) { conn.close(); }
	    	}catch(SQLException se) {

	    	}
	        
	    }
		return new ResponseEntity(usersArray.toString(), responseHeaders, HttpStatus.OK);
	}

	@RequestMapping(value = "/spend", method = RequestMethod.POST) // <-- setup the endpoint URL at /hello with the HTTP POST method
	public ResponseEntity<String> spend(@RequestBody String payload, HttpServletRequest request) {
		JSONObject payloadObj = new JSONObject(payload);
		String username = payloadObj.getString("username"); 

		HttpHeaders responseHeaders = new HttpHeaders(); 
    	responseHeaders.set("Content-Type", "application/json");
		Connection conn = null;
		JSONArray usersArray = new JSONArray();

		String auth = payloadObj.getString("auth");
		if(!auth.equals(MyServer.users.get(username)))
		{
			return new ResponseEntity("{\"message\":\"Log Out\"}", responseHeaders, HttpStatus.BAD_REQUEST);
		}

	    try {
	    	conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/charity?useUnicode=true&characterEncoding=UTF-8", "root", "snowleopard");
			String query = "UPDATE charity.users SET users.balance = users.balance-.? WHERE users.username = ?";
			PreparedStatement stmt = null;
			stmt = conn.prepareStatement(query);
			stmt.setInt(1,(int)(Math.random() * 100));
			stmt.setString(2,username);
			stmt.executeUpdate();


			query = "SELECT users.balance FROM charity.users WHERE username = ?";
	        stmt = conn.prepareStatement(query);
	        stmt.setString(1, username);

	        ResultSet rs = stmt.executeQuery();
			
			while (rs.next()) {
	            Double balance = rs.getDouble("users.balance");

	            JSONObject obj = new JSONObject();
	            obj.put("Balance", balance);
	            usersArray.put(obj);
			   }
	        
	    } catch (Exception e ) {
	    	return new ResponseEntity("{\"message\":\"Error. Try Again\"}", responseHeaders, HttpStatus.BAD_REQUEST);
	    } finally {
	    	try {
	    		if (conn != null) { conn.close(); }
	    	}catch(SQLException se) {

	    	}
	        
	    }
		return new ResponseEntity(usersArray.toString(), responseHeaders, HttpStatus.OK);
	}

	@RequestMapping(value = "/donate", method = RequestMethod.POST) // <-- setup the endpoint URL at /hello with the HTTP POST method
	public ResponseEntity<String> donate(@RequestBody String payload, HttpServletRequest request) {
		JSONObject payloadObj = new JSONObject(payload);
		String username = payloadObj.getString("username"); 
		Double cents = Double.parseDouble(payloadObj.getString("cents"));
		String charity = payloadObj.getString("charityName");
		Double totalDonated = 0.0;

		HttpHeaders responseHeaders = new HttpHeaders(); 
    	responseHeaders.set("Content-Type", "application/json");
		Connection conn = null;
		JSONArray usersArray = new JSONArray();

		String auth = payloadObj.getString("auth");
		if(!auth.equals(MyServer.users.get(username)))
		{
			return new ResponseEntity("{\"message\":\"Log Out\"}", responseHeaders, HttpStatus.BAD_REQUEST);
		}

	    try {
	    	conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/charity?useUnicode=true&characterEncoding=UTF-8", "root", "snowleopard");
			String query = "UPDATE charity.users SET users.balance = users.balance-? WHERE users.username = ?";
			PreparedStatement stmt = null;
	        stmt = conn.prepareStatement(query);
	        stmt.setDouble(1, cents);
	        stmt.setString(2, username);
	        stmt.executeUpdate();

	        query = "UPDATE charity.users SET users.donatedTotal = users.donatedTotal+? WHERE users.username =?";
	        stmt = conn.prepareStatement(query);
	        stmt.setDouble(1, cents);
	        stmt.setString(2, username);
	        stmt.executeUpdate();

	        query = "INSERT INTO charity.transactions VALUES(?,?,?,?)";
	        stmt = conn.prepareStatement(query);
	        stmt.setDouble(3, cents);
	        stmt.setString(1, username);
	        stmt.setString(2, charity);
	        stmt.setTimestamp(4, java.sql.Timestamp.from(java.time.Instant.now()));
	        stmt.executeUpdate();

	        query = "UPDATE charity.charities SET charities.donatesTotal = charities.donatesTotal+? WHERE charities.charity =?";
	        stmt = conn.prepareStatement(query);
	        stmt.setDouble(1, cents);
	        stmt.setString(2, charity);
	        stmt.executeUpdate();

	        query = "SELECT users.donatedTotal FROM charity.users WHERE users.username = ?";
			stmt = null;
	        stmt = conn.prepareStatement(query);
	        stmt.setString(1, username);
	        ResultSet rs = stmt.executeQuery();
	        
	        while (rs.next()) {
	           totalDonated = rs.getDouble("users.donatedTotal");
	        }
	    	} catch (Exception e ) {
	    	return new ResponseEntity("{\"message\":\"Failed Donation\"}", responseHeaders, HttpStatus.BAD_REQUEST);
	    } finally {
	    	try {
	    		if (conn != null) { conn.close(); }
	    	}catch(SQLException se) {
	    	}
	    }
		return new ResponseEntity(totalDonated, responseHeaders, HttpStatus.OK);
	}

	@RequestMapping(value = "/quickSearch", method = RequestMethod.POST) // <-- setup the endpoint URL at /hello with the HTTP POST method
	public ResponseEntity<String> quickSearch(@RequestBody String payload, HttpServletRequest request) {
		
		JSONObject payloadObj = new JSONObject(payload);
		String charityName = payloadObj.getString("charityName"); //Grabbing name and age parameters from URL
		String username = payloadObj.getString("username");
		HttpHeaders responseHeaders = new HttpHeaders(); 
    	responseHeaders.set("Content-Type", "application/json");
		Connection conn = null;
		JSONArray usersArray = new JSONArray();

		String auth = payloadObj.getString("auth");
		if(!auth.equals(MyServer.users.get(username)))
		{
			return new ResponseEntity("{\"message\":\"Log Out\"}", responseHeaders, HttpStatus.BAD_REQUEST);
		}

	    try {
	    	conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/charity?useUnicode=true&characterEncoding=UTF-8", "root", "snowleopard");
			String query = "SELECT charities.charity, charities.subscribers, charities.donatesTotal FROM charity.charities WHERE charities.charity = ?";
			PreparedStatement stmt = null;
	        stmt = conn.prepareStatement(query);
	        stmt.setString(1, charityName);
	        ResultSet rs = stmt.executeQuery();
	        
	        while (rs.next()) {
	            String name = rs.getString("charities.charity");
	            int subs = rs.getInt("charities.subscribers");
	            Double totalDonates = rs.getDouble("charities.donatesTotal");

	            JSONObject obj = new JSONObject();
	            obj.put("Charity Name", name);
	            obj.put("Subscribers", subs);
	            obj.put("Total Donates", totalDonates);
	            usersArray.put(obj);
	        
	    } 
	}catch (Exception e ) {
	    	return new ResponseEntity("{\"message\":\"Error. Try Again\"}", responseHeaders, HttpStatus.BAD_REQUEST);
	    } finally {
	    	try {
	    		if (conn != null) { conn.close(); }
	    	}catch(SQLException se) {

	    	}
	        
	    }
		return new ResponseEntity(usersArray.toString(), responseHeaders, HttpStatus.OK);
	}

	@RequestMapping(value = "/searchSort", method = RequestMethod.POST) // <-- setup the endpoint URL at /hello with the HTTP POST method
	public ResponseEntity<String> searchSort(@RequestBody String payload, HttpServletRequest request) {
		
		JSONObject payloadObj = new JSONObject(payload);
		String sortBy = payloadObj.getString("search"); //Grabbing name and age parameters from URL
		String username = payloadObj.getString("username");
		HttpHeaders responseHeaders = new HttpHeaders(); 
    	responseHeaders.set("Content-Type", "application/json");
		Connection conn = null;
		JSONArray usersArray = new JSONArray();

		String auth = payloadObj.getString("auth");
		if(!auth.equals(MyServer.users.get(username)))
		{
			return new ResponseEntity("{\"message\":\"Log Out\"}", responseHeaders, HttpStatus.BAD_REQUEST);
		}

	    try {
	    	conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/charity?useUnicode=true&characterEncoding=UTF-8", "root", "snowleopard");
			PreparedStatement stmt = null;
			ResultSet rs = null;
			if(sortBy.equals("Name"))
			{
				String query = "SELECT charities.charity, charities.subscribers, charities.donatesTotal FROM charity.charities ORDER BY charities.charity";
		        stmt = conn.prepareStatement(query);
		        rs = stmt.executeQuery();
	    	}
	    	else if(sortBy.equals("Most Donated"))
	    	{
	    		String query = "SELECT charities.charity, charities.subscribers, charities.donatesTotal FROM charity.charities ORDER BY charities.donatesTotal DESC";
		        stmt = conn.prepareStatement(query);
		        rs = stmt.executeQuery();
	    	}
	    	else if(sortBy.equals("Most Subscribers"))
	    	{
	    		String query = "SELECT charities.charity, charities.subscribers, charities.donatesTotal FROM charity.charities ORDER BY charities.subscribers DESC";
		        stmt = conn.prepareStatement(query);
		        rs = stmt.executeQuery();
	    	}
	    	else if(sortBy.equals("My List"))
	    	{
	    		String query = "SELECT distinct subscribers.charity, charities.subscribers, charities.donatesTotal FROM charity.charities, charity.subscribers WHERE subscribers.username = ? AND subscribers.charity = charities.charity";
		        stmt = conn.prepareStatement(query);
		        stmt.setString(1, username);
		        rs = stmt.executeQuery();
	    	}
	        
	        while (rs.next()) {
	            String charityName = rs.getString("charity");
	            int subs = rs.getInt("charities.subscribers");
	            Double totalDonates = rs.getDouble("charities.donatesTotal");

	            JSONObject obj = new JSONObject();
	            obj.put("Total Donates", totalDonates);
	            obj.put("Subscribers", subs);
	            obj.put("Charity Name", charityName);
	            usersArray.put(obj);
	        
	    } 
	}catch (Exception e ) {
	    	return new ResponseEntity("{\"message\":\"Error. Try Again\"}", responseHeaders, HttpStatus.BAD_REQUEST);
	    } finally {
	    	try {
	    		if (conn != null) { conn.close(); }
	    	}catch(SQLException se) {

	    	}
	        
	    }
		return new ResponseEntity(usersArray.toString(), responseHeaders, HttpStatus.OK);
	}

	@RequestMapping(value = "/searchSortCharity", method = RequestMethod.POST) // <-- setup the endpoint URL at /hello with the HTTP POST method
	public ResponseEntity<String> searchSortCharity(@RequestBody String payload, HttpServletRequest request) {
		
		JSONObject payloadObj = new JSONObject(payload);
		String sortBy = payloadObj.getString("search"); //Grabbing name and age parameters from URL
		String username = payloadObj.getString("username");
		HttpHeaders responseHeaders = new HttpHeaders(); 
    	responseHeaders.set("Content-Type", "application/json");
		Connection conn = null;
		JSONArray usersArray = new JSONArray();

		String auth = payloadObj.getString("auth");
		if(!auth.equals(MyServer.users.get(username)))
		{
			return new ResponseEntity("{\"message\":\"Log Out\"}", responseHeaders, HttpStatus.BAD_REQUEST);
		}

	    try {
	    	conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/charity?useUnicode=true&characterEncoding=UTF-8", "root", "snowleopard");
			PreparedStatement stmt = null;
			ResultSet rs = null;
			if(sortBy.equals("Name"))
			{
				String query = "SELECT charities.charity, charities.subscribers, charities.donatesTotal FROM charity.charities ORDER BY charities.charity";
		        stmt = conn.prepareStatement(query);
		        rs = stmt.executeQuery();
	    	}
	    	else if(sortBy.equals("Most Donated"))
	    	{
	    		String query = "SELECT charities.charity, charities.subscribers, charities.donatesTotal FROM charity.charities ORDER BY charities.donatesTotal DESC";
		        stmt = conn.prepareStatement(query);
		        rs = stmt.executeQuery();
	    	}
	    	else if(sortBy.equals("Most Subscribers"))
	    	{
	    		String query = "SELECT charities.charity, charities.subscribers, charities.donatesTotal FROM charity.charities ORDER BY charities.subscribers DESC";
		        stmt = conn.prepareStatement(query);
		        rs = stmt.executeQuery();
	    	}
	    	else if(sortBy.equals("My List"))
	    	{

	    	}
	        
	        while (rs.next()) {
	            String charityName = rs.getString("charity");
	            int subs = rs.getInt("charities.subscribers");
	            Double totalDonates = rs.getDouble("charities.donatesTotal");

	            JSONObject obj = new JSONObject();
	            obj.put("Total Donates", totalDonates);
	            obj.put("Subscribers", subs);
	            obj.put("Charity Name", charityName);
	            usersArray.put(obj);
	        
	    } 
	}catch (Exception e ) {
	    	return new ResponseEntity("{\"message\":\"Error. Try Again\"}", responseHeaders, HttpStatus.BAD_REQUEST);
	    } finally {
	    	try {
	    		if (conn != null) { conn.close(); }
	    	}catch(SQLException se) {

	    	}
	        
	    }
		return new ResponseEntity(usersArray.toString(), responseHeaders, HttpStatus.OK);
	}

	@RequestMapping(value = "/addCharity", method = RequestMethod.POST) // <-- setup the endpoint URL at /hello with the HTTP POST method
	public ResponseEntity<String> addCharity(@RequestBody String payload, HttpServletRequest request) {
		JSONObject payloadObj = new JSONObject(payload);
		String charityname = payloadObj.getString("charityName"); 
		String username = payloadObj.getString("username");

		HttpHeaders responseHeaders = new HttpHeaders(); 
    	responseHeaders.set("Content-Type", "application/json");
		Connection conn = null;
		JSONArray usersArray = new JSONArray();

		String auth = payloadObj.getString("auth");
		if(!auth.equals(MyServer.users.get(username)))
		{
			return new ResponseEntity("{\"message\":\"Log Out\"}", responseHeaders, HttpStatus.BAD_REQUEST);
		}

	    try {
	    	conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/charity?useUnicode=true&characterEncoding=UTF-8", "root", "snowleopard");
			String query = "INSERT INTO charity.subscribers(username,charity) VALUES (?,?)";
			PreparedStatement stmt = null;
	        stmt = conn.prepareStatement(query);
	        stmt.setString(1, username);
	        stmt.setString(2, charityname);
	        stmt.executeUpdate();

	        query = "UPDATE charity.charities SET charities.subscribers = charities.subscribers+1 WHERE charities.charity = ?";
	        stmt = conn.prepareStatement(query);
	        stmt.setString(1, charityname);
	        stmt.executeUpdate();

	    } catch  (Exception e) {
	    	return new ResponseEntity("{\"message\":\"Error. Try Again\"}", responseHeaders, HttpStatus.BAD_REQUEST);
	    } finally {
	    	try {
	    		if (conn != null) { conn.close(); }
	    	}catch(SQLException se) {

	    	}
	        
	    }
		return new ResponseEntity(payloadObj.toString(), responseHeaders, HttpStatus.OK);
	}

	@RequestMapping(value = "/removeCharity", method = RequestMethod.POST) // <-- setup the endpoint URL at /hello with the HTTP POST method
	public ResponseEntity<String> removeCharity(@RequestBody String payload, HttpServletRequest request) {
		JSONObject payloadObj = new JSONObject(payload);
		String charityname = payloadObj.getString("charityName"); 
		String username = payloadObj.getString("username");

		HttpHeaders responseHeaders = new HttpHeaders(); 
    	responseHeaders.set("Content-Type", "application/json");
		Connection conn = null;

		String auth = payloadObj.getString("auth");
		if(!auth.equals(MyServer.users.get(username)))
		{
			return new ResponseEntity("{\"message\":\"Log Out\"}", responseHeaders, HttpStatus.BAD_REQUEST);
		}

	    try {
	    	conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/charity?useUnicode=true&characterEncoding=UTF-8", "root", "snowleopard");
			
			String query = "SELECT * FROM charity.subscribers WHERE username = ? AND charity = ?";
			PreparedStatement stmt = conn.prepareStatement(query);
			stmt.setString(1, username);
			stmt.setString(2, charityname);
			ResultSet rs = stmt.executeQuery();
			
			while (rs.next()) {
			    String name = rs.getString("subscribers.charity");
			    if(name.equals(charityname))
			    {
			    	query = "UPDATE charity.charities SET charities.subscribers = charities.subscribers-1 WHERE charities.charity = ?";
					stmt = conn.prepareStatement(query);
					stmt.setString(1, charityname);
					stmt.executeUpdate();
			    }
			   }

			query = "DELETE FROM charity.subscribers WHERE subscribers.username=? AND subscribers.charity=?";
	        stmt = conn.prepareStatement(query);
	        stmt.setString(1, username);
	        stmt.setString(2, charityname);
	        stmt.executeUpdate();

	        
	        
	    } catch  (Exception e) {
	    	return new ResponseEntity("{\"message\":\"Error. Try Again\"}", responseHeaders, HttpStatus.BAD_REQUEST);
	    } finally {
	    	try {
	    		if (conn != null) { conn.close(); }
	    	}catch(SQLException se) {

	    	}
	        
	    }
		return new ResponseEntity(payloadObj.toString(), responseHeaders, HttpStatus.OK);
	}

	public static String bytesToHex(byte[] in) {
		StringBuilder builder = new StringBuilder();
		for(byte b: in) {
			builder.append(String.format("%02x", b));
		}
		return builder.toString();
	}

}

class ServerListener extends Thread {
    Socket socket;
    ServerListener(Socket socket) {
        this.socket = socket;
    }
    public void run() {
        try {	
            BufferedReader in =
                new BufferedReader(
                    new InputStreamReader(socket.getInputStream())); //Input stream from server
            String serverInput;
            while((serverInput = in.readLine()) != null) {
                System.out.println(serverInput); //Print input from server into console

            }
        }catch(IOException ie) {

        }
    }
}


