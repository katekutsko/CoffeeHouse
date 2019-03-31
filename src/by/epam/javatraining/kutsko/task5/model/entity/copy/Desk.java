package by.epam.javatraining.kutsko.task5.model.entity.copy;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import by.epam.javatraining.kutsko.task5.model.exception.NotEnoughMoneyException;
import by.epam.javatraining.kutsko.task5.model.exception.OutOfCoffeeException;

/**
 * 
 * @author
 *
 */
public class Desk {

	private Cafe cafe;
	private int id;
	private boolean free;

	private AtomicInteger order;

	private Queue<Client> queue;
	private Lock lock;

	{
		queue = new LinkedList<>();
		lock = new ReentrantLock();
		order = new AtomicInteger(0);
	}

	public Desk() {
		free = true;
		id = 0;
	}

	public Desk(Cafe cafe, int id) {
		this.cafe = cafe;
		free = true;
		this.id = id;
	}

	public boolean isFree() {
		return free;
	}

	public void changeAvailability(boolean flag) {
		free = flag;
	}

	/**
	 * 
	 * @param money that the client has; exchanged for coffee
	 * @return cost of the order
	 * @throws OutOfCoffeeException    when there is no coffee left in cafe
	 * @throws NotEnoughMoneyException when client doesn't have enough money
	 */
	public AtomicInteger serveCustomers(AtomicInteger money) throws OutOfCoffeeException, NotEnoughMoneyException {

		if (lock.tryLock()) {

			if (cafe.getCupsOfCoffee() > 0) {
				if (money.get() >= Cafe.PRICE_OF_ONE_CUP) {
					try {
						Thread.sleep(500);
						order = new AtomicInteger(cafe.sellCoffee(money.get()));
						// TODO
						System.out.println(
								order + " cups were sold to customer. " + cafe.getCupsOfCoffee() + " cups left.");

					} catch (InterruptedException e) {
						// TODO LOGGER.error(e);
						System.out.println(e);
					} finally {
						lock.unlock();
					}
					return new AtomicInteger(order.get() * Cafe.PRICE_OF_ONE_CUP);
				}
				throw new NotEnoughMoneyException(
						"Client has " + money.get() + " dollars, while coffee costs " + Cafe.PRICE_OF_ONE_CUP);
			}
			lock.unlock();
			throw new OutOfCoffeeException();
		}
		return new AtomicInteger(0);
		// TODO Probably there will be some way of adding clients to queue
	}

	public AtomicInteger getOrder() {
		return order;
	}

	public int getId() {
		return id;
	}
}
