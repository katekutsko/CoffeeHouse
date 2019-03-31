package by.epam.javatraining.kutsko.task5.model.entity;

import java.util.Random;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;

public class Client implements Runnable {

	private int id;

	private Cafe cafe;

	private Desk desk;
	private volatile State state;

	private static final Random random;
	private static final Logger LOGGER;
	private static final int WAIT_BEFORE_SEARCH_FOR_NEW_QUEUE = 3000;
	private static final int MINIMAL_WAITING_TIME = 1000;
	private static final String FILE_NAME = "resource/log4j.xml";

	public enum State {
		ENTERED, WAITING, BEING_SERVICED, SERVICED, LEFT
	}

	private Thread thread;

	private AtomicInteger money;
	private AtomicInteger cupsOfCoffee;

	{
		state = State.ENTERED;
		cupsOfCoffee = new AtomicInteger(0);

	}

	static {
		LOGGER = Logger.getRootLogger();
		DOMConfigurator.configure(FILE_NAME);
		random = new Random();
	}

	public Client() {
		this.money = new AtomicInteger(0);
		this.id = 0;
		thread.start();
	}

	public Client(Cafe cafe, int money, int id) {
		this.cafe = cafe;
		this.money = new AtomicInteger(money);
		this.id = id;
		thread = new Thread(this, "Client " + id);
		thread.start();
	}

	public void changeState(State state) {
		if (state != null) {
			this.state = state;
		} else {
			LOGGER.warn("State for client " + id + " was null");
		}
	}

	public Thread getThread() {
		return thread;
	}

	public int getId() {
		return id;
	}

	public AtomicInteger getMoney() {
		return money;
	}

	public Desk getQueue() {
		return desk;
	}
	
	public State getState() {
		return state;
	}


	@Override
	public void run() {

		if (cafe != null) {

			desk = cafe.findShortestQueue(this);
		}

		while (state == State.WAITING || state == State.BEING_SERVICED) {

			try {
				Thread.sleep(random.nextInt(WAIT_BEFORE_SEARCH_FOR_NEW_QUEUE) + MINIMAL_WAITING_TIME);
			} catch (InterruptedException e) {

				desk.leaveQueue(this);
				LOGGER.error("Something happened with client " + id);
			}

			if (state == State.WAITING) {
				desk = cafe.findShortestQueue(this);
			} else if (state == State.SERVICED){
				break;
			}
			
			if (!cafe.isOpened()) {
				break;
			}
		}
		state = State.LEFT;
		LOGGER.info("Client " + id + " left cafe");
	}

	public void acceptOrder(AtomicInteger order) {

		if (order != null) {

			cupsOfCoffee = order;
			money.updateAndGet(n -> n - cupsOfCoffee.get() * Cafe.PRICE_OF_ONE_CUP);

			LOGGER.info("Client " + id + " has " + cupsOfCoffee + " cup(s) and " + money + " dollars left");
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Client other = (Client) obj;
		if (id != other.id)
			return false;
		return true;
	}

}
