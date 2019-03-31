package by.epam.javatraining.kutsko.task5.model.entity.copy;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import by.epam.javatraining.kutsko.task5.model.exception.CashierServiceException;
import by.epam.javatraining.kutsko.task5.model.exception.NotEnoughMoneyException;
import by.epam.javatraining.kutsko.task5.model.exception.OutOfCoffeeException;
import by.epam.javatraining.kutsko.task5.model.exception.WaitingTimeExpiredException;

public class Client implements Runnable {

	private int id;
	private Cafe cafe;
	private Desk desk;

	private Thread thread;

	private AtomicInteger money;
	private AtomicInteger cupsOfCoffee;

	{
		cupsOfCoffee = new AtomicInteger(0);
		thread = new Thread(this);
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
		thread.start();
	}
	
	public Thread getThread() {
		return thread;
	}
	
	public int getId() {
		return id;
	}

	@Override
	public void run() {
		
		System.out.println("client "+ id+" entered the cafe");
		Random random = new Random();
		
		if (cafe != null) {
			try {
				desk = cafe.takeDesk(random.nextInt(10), this);
				if (desk != null) {
					System.out.println("desk " + desk.getId() + " is taken by customer " + id);
					desk.changeAvailability(false);
					AtomicInteger spentMoney = desk.serveCustomers(money);
					cupsOfCoffee.addAndGet(desk.getOrder().get());
					System.out.println("client " + id + " has " + cupsOfCoffee.get() + " cups");
					money.addAndGet(-spentMoney.get());
				}
			} catch (CashierServiceException e) {
				// TODO LOGGER.error();
				System.out.println(e);
			} catch (WaitingTimeExpiredException e) {
				// TODO Later when I add queue there will be choice: just leave or let someone
				// else take place
				System.out.println(e);
			} catch (OutOfCoffeeException e) {
				// TODO LOGGER.warn();
				System.out.println(e);
				return;
			} catch (NotEnoughMoneyException e) {
				// TODO LOGGER.warn();
				System.out.println(e);
				return;
			} finally {
				if (desk != null) {
					cafe.freeDesk(desk);
					System.out.println("desk " + desk.getId() + " is freed by customer " + id);
				}
			}
		}
	}
}
