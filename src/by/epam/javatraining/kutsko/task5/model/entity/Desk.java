package by.epam.javatraining.kutsko.task5.model.entity;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import by.epam.javatraining.kutsko.task5.model.entity.Client.State;
import by.epam.javatraining.kutsko.task5.model.exception.OutOfCoffeeException;

/**
 * 
 * @author Kate Kutsko
 *
 */
public class Desk implements Runnable {

	public static final int MAX_QUEUE_LENGTH = 10;
	public static final int WAIT_FOR_NEW_CLIENT = 10000;
	private static final String FILE_NAME = "resource/log4j.xml";
	private final static Logger LOGGER;

	private Thread thread;

	private Cafe cafe;
	private int id;

	private AtomicInteger order;

	private ArrayBlockingQueue<Client> queue;
	private Lock lock;

	{
		thread = new Thread(this);
		queue = new ArrayBlockingQueue<Client>(10, true);
		lock = new ReentrantLock();
		order = new AtomicInteger(0);
	}

	static {
		LOGGER = Logger.getRootLogger();
		DOMConfigurator.configure(FILE_NAME);
	}

	public Desk() {}

	public Desk(Cafe cafe, int id) {
		this.cafe = cafe;
		this.id = id;
	}

	public void run() {

		while (true) {

			Client client = null;

			try {
				client = queue.poll(WAIT_FOR_NEW_CLIENT, TimeUnit.MILLISECONDS);

				if (client != null) {
					
					client.changeState(State.BEING_SERVICED);
					AtomicInteger money = client.getMoney();

					LOGGER.info("Client " + client.getId() + " (" + money + "$)" + " is being served at desk " + id);

					if (money.get() >= Cafe.PRICE_OF_ONE_CUP) {

						try {
							order = cafe.sellCoffee(money);
							client.acceptOrder(order);
							
						} catch (OutOfCoffeeException e) {
							
							client.changeState(State.SERVICED);
							LOGGER.info("Desk " + id + " is out of service (no coffee left)");
							break;

						} finally {
							client.changeState(State.SERVICED);
						}

					} else {
						client.changeState(State.SERVICED);
						LOGGER.warn("Client " + client.getId() + " left without service (not enough money) ");
					}
				} else {

					LOGGER.info("Desk " + id + " is out of service (no clients left)");
					break;
				}
			} catch (InterruptedException e) {
				LOGGER.error("Cash desk thread was interrupted (id = " + id + ")");
			}

		}
	}

	public boolean joinQueue(Client client) {

		lock.lock();

		boolean added = false;

		try {
			if (client != null && (client.getState() == State.WAITING) || (client.getState() == State.ENTERED)) {

				try {
					added = queue.offer(client, WAIT_FOR_NEW_CLIENT, TimeUnit.MILLISECONDS);
					client.changeState(State.WAITING);
				} catch (InterruptedException e) {
					LOGGER.error("Thread at desk " + id + " interrupted");
				}
				if (added) {
					LOGGER.info("Client " + client.getId() + " joined queue "  + id);
				}
				return added;

			} else {
				return false;
			}
		} finally {
			lock.unlock();
		}
	}

	public boolean leaveQueue(Client client) {

		lock.lock();

		try {
			if (client != null) {
				LOGGER.info("Client " + client.getId() + " left queue "  + id);
				return queue.remove(client);
			}
			return false;

		} finally {
			lock.unlock();
		}
	}

	public Thread getThread() {
		return thread;
	}

	public AtomicInteger getOrder() {
		return order;
	}

	public int getId() {
		return id;
	}
	
	public int getClientPosition(Client client) {
		
		if (client != null && queue.contains(client)) {
			int count = 0;
			for (Client otherClient: queue) {
				if (client.equals(otherClient)) {
					return count;
				}
				count++;
			}
		}
		return -1;
	}

	public int getQueueLength() {
		try {
			lock.lock();
			int size = queue.size();
			return size;
		} finally {
			lock.unlock();
		}
	}
}
