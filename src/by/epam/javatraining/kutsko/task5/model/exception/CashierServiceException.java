package by.epam.javatraining.kutsko.task5.model.exception;

public class CashierServiceException extends Exception {

	public CashierServiceException() {
	}

	public CashierServiceException(String message) {
		super(message);
	}

	public CashierServiceException(Throwable cause) {
		super(cause);
	}

	public CashierServiceException(String message, Throwable cause) {
		super(message, cause);
	}
}
