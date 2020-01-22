package MyServer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashMap;

import java.net.*;
import java.io.*;
import java.util.*;

@SpringBootApplication
public class MyServer {
	public static HashMap<String, String> users = new HashMap<>();
	public static ArrayList<User> tokensArrayList = new ArrayList<>();

	public static void main(String[] args) {
		SpringApplication.run(MyServer.class, args);
	}
}


class User{
	String username;
	String token;
	User(String username, String token)
	{
		this.username = username;
		this.token=token;
	}
}