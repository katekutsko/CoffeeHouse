package by.epam.javatraining.kutsko.task5.util.creator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import by.epam.javatraining.kutsko.task5.model.entity.Cafe;
import by.epam.javatraining.kutsko.task5.model.entity.Client;

public class ClientGenerator {
	
	public static final int DELAY = 1000;
	public static final int MAX_ADDITIONAL_CASH = 20;
	public static final int MIN_CLIENT_CASH = 10;
	
	
	public static List<Client> inviteClients(Cafe cafe, int amountOfClients){
		
		List<Client> allClients = new ArrayList<>();
		
		for (int i = 0; i < amountOfClients; i++) {
			
			try {
				Thread.sleep(new Random().nextInt(DELAY));
			} catch (InterruptedException e) {}
			
			allClients.add(new Client(cafe, new Random().nextInt(MAX_ADDITIONAL_CASH) + MIN_CLIENT_CASH, i));
		}
		return allClients;
	}
}
