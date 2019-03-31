package by.epam.javatraining.kutsko.task5.controller;

import by.epam.javatraining.kutsko.task5.model.entity.*;
import by.epam.javatraining.kutsko.task5.util.creator.ClientGenerator;

public class Controller {
	
	private static int amountOfClients = 40;

	public static void main(String[] args) {
		
		Cafe cafe = Cafe.getInstance();
		
		cafe.openCafe();	
		
		ClientGenerator.inviteClients(cafe, amountOfClients);
		
	}

}
