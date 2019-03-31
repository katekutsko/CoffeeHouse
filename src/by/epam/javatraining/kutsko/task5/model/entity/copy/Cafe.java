package by.epam.javatraining.kutsko.task5.model.entity.copy;
/**
 * 
 * @author 
 *
 */

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import by.epam.javatraining.kutsko.task5.model.exception.CashierServiceException;
import by.epam.javatraining.kutsko.task5.model.exception.OutOfCoffeeException;
import by.epam.javatraining.kutsko.task5.model.exception.WaitingTimeExpiredException;

public class Cafe {

	public static final int DESK_AMOUNT = 5;
	public static final int PRICE_OF_ONE_CUP = 10;

	private AtomicInteger cupsOfCoffee;
	private AtomicInteger profit;

	private static Cafe instance;

	private static Lock lock = new ReentrantLock();
	private Semaphore semaphore = new Semaphore(DESK_AMOUNT, true);
	private Queue<Desk> desks = new LinkedList<>();

	{
		for (int i = 0; i < DESK_AMOUNT; i++) {
			desks.add(new Desk(this, i));
		}
	}

	private Cafe(int cupsOfCoffee) {
		this.cupsOfCoffee = new AtomicInteger(cupsOfCoffee);
		profit = new AtomicInteger(0);
	}

	public static Cafe getInstance(int cupsOfCoffee) {
		
		lock.lock();

		if (instance == null) {
			instance = new Cafe(cupsOfCoffee);
		}
		lock.unlock();
		return instance;
	}

	public Desk takeDesk(long waitingTime, Client client) throws CashierServiceException, WaitingTimeExpiredException {
		
		try {
			if (semaphore.tryAcquire(waitingTime, TimeUnit.SECONDS)) {
				System.out.println("client " + client.getId() + " got a desk.");
				return desks.poll();
			} else {
				// TODO
				System.out.println("client " + client.getId() + " couldn't wait anymore and left.");
			}
		} catch (InterruptedException e) {
			throw new CashierServiceException(e);
		}
		throw new WaitingTimeExpiredException();
	}

	public void freeDesk(Desk desk) {
		desks.add(desk);
		System.out.println("desk " + desk.getId() + " was freed");
		semaphore.release();
	}

	public int getCupsOfCoffee() {
		return cupsOfCoffee.get();
	}

	public int sellCoffee(int money) throws OutOfCoffeeException {
		
		try {
			if (lock.tryLock(500, TimeUnit.MILLISECONDS)) {
				
				if (!(cupsOfCoffee.get() == 0)) {
					int orderSize = money / PRICE_OF_ONE_CUP;

					if (orderSize <= cupsOfCoffee.get()) {
						cupsOfCoffee.updateAndGet((n -> n - orderSize));
						lock.unlock();
						return orderSize;

					} else {
						int newOrderSize = cupsOfCoffee.get();
						cupsOfCoffee.updateAndGet((n -> n - newOrderSize));
						lock.unlock();
						return newOrderSize;
					}
				} else {
					lock.unlock();
					throw new OutOfCoffeeException();
				}
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			System.out.println(e);
		}
		return 0;
	}
}
