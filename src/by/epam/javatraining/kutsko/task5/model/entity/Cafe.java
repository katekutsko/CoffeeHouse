package by.epam.javatraining.kutsko.task5.model.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import by.epam.javatraining.kutsko.task5.model.exception.CashierServiceException;
import by.epam.javatraining.kutsko.task5.model.exception.NotEnoughMoneyException;
import by.epam.javatraining.kutsko.task5.model.exception.OutOfCoffeeException;
import by.epam.javatraining.kutsko.task5.model.exception.WaitingTimeExpiredException;

/**
 * This class simulates a coffee house. Sells coffee, gets profit in exchange.
 * Has a certain amount of desks that serve clients.
 * 
 * @author Kate Kutsko
 */
public class Cafe {

	public static final int DESK_AMOUNT = 3;
	public static final int PRICE_OF_ONE_CUP = 10;
	public static final int COOL_OFF_PERIOD = 1000;
	private static final String FILE_NAME = "resource/log4j.xml";
	private static final int STOCK_AMOUNT = 30;
	private static final String CAFE_NAME = "CoffeeBox";

	private static Cafe instance;
	private static Lock lock;
	private static Logger LOGGER;

	private AtomicInteger cupsOfCoffee;
	private AtomicInteger profit;

	private String name;

	private List<Desk> desks = new ArrayList<>();

	static {
		lock = new ReentrantLock();
		LOGGER = Logger.getRootLogger();
		DOMConfigurator.configure(FILE_NAME);
	}

	{
		for (int i = 0; i < DESK_AMOUNT; i++) {
			desks.add(new Desk(this, i));
		}
		profit = new AtomicInteger(0);
	}

	private Cafe() {
		name = "";
		cupsOfCoffee = new AtomicInteger(0);
	}

	private Cafe(int cupsOfCoffee, String name) {
		this.name = name;
		this.cupsOfCoffee = new AtomicInteger(cupsOfCoffee);
	}

	public static Cafe getInstance() {
		lock.lock();
		try {
			if (instance == null) {
				instance = new Cafe(STOCK_AMOUNT, CAFE_NAME);
			}
			return instance;
		} finally {
			lock.unlock();
		}
	}

	public void setDesks(List<Desk> desks) {
		if (desks != null) {
			this.desks = desks;
		}
	}

	public void setStocks(int cupsOfCoffee) {
		this.cupsOfCoffee = new AtomicInteger(cupsOfCoffee);
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void openCafe() {
		for (Desk desk : desks) {
			if (desk != null) {
				desk.getThread().start();
			}
		}
	}
	
	public boolean isOpened() {
		return cupsOfCoffee.get() != 0;
	}
	
	public Desk findShortestQueue(Client client) {

		lock.lock();

		try {
			if (cupsOfCoffee.get() == 0) {
				return null;
			}
			
			if (client != null) {

				Desk clientQueue = client.getQueue();
				Desk minQueue = null;

				if (clientQueue != null) {

					for (Desk desk : desks) {

						if (desk.getQueueLength() < clientQueue.getClientPosition(client)) {
							minQueue = desk;
						}
					}
					if (minQueue != null) {

						if (minQueue != clientQueue) {

							clientQueue.leaveQueue(client);
							minQueue.joinQueue(client);

							LOGGER.info("Client " + client.getId() + " left queue " + clientQueue.getId()
									+ " and joined queue " + minQueue.getId());
						} else {

							LOGGER.info(
									"Client " + client.getId() + " wanted to change queue, but " + clientQueue.getId()
											+ " and " + minQueue.getId() + " had equal lengths of queues");
						}
						return minQueue;
					}
				} else {

					minQueue = desks.get(0);

					for (Desk desk : desks) {

						if (desk.getQueueLength() < minQueue.getQueueLength()) {
							minQueue = desk;
						}
					}
					minQueue.joinQueue(client);
					return minQueue;
				}
			}
			return null;

		} finally {
			lock.unlock();
		}

	}

	public AtomicInteger sellCoffee(AtomicInteger money) throws OutOfCoffeeException {

		lock.lock();

		try {
			if (!(cupsOfCoffee.get() == 0)) {

				AtomicInteger orderSize = new AtomicInteger(money.get() / PRICE_OF_ONE_CUP);

				try {
					Thread.sleep(COOL_OFF_PERIOD);
				} catch (InterruptedException e) {
					LOGGER.error("Cafe thread was interrupted");
				}

				if (orderSize.get() <= cupsOfCoffee.get()) {

					cupsOfCoffee.updateAndGet((n -> n - orderSize.get()));
					profit.updateAndGet(n -> n + orderSize.get() * PRICE_OF_ONE_CUP);

					return orderSize;

				} else {

					AtomicInteger newOrderSize = cupsOfCoffee;
					cupsOfCoffee.updateAndGet((n -> n - newOrderSize.get()));
					profit.updateAndGet(n -> n + newOrderSize.get() * PRICE_OF_ONE_CUP);

					return newOrderSize;
				}

			} else {
				throw new OutOfCoffeeException();
			}
		} finally {
			LOGGER.info("Cafe's profit is currently " + profit + " and " + cupsOfCoffee + " cups left in storage");
			lock.unlock();
			try {
				Thread.sleep(COOL_OFF_PERIOD);
			} catch (InterruptedException e) {
			}
		}
	}

	public AtomicInteger getCupsOfCoffee() {
		return cupsOfCoffee;
	}

	public List<Desk> getDesks() {
		lock.lock();
		List<Desk> desks = this.desks;
		lock.unlock();
		return desks;
	}

	public AtomicInteger getProfit() {
		return profit;
	}

}
