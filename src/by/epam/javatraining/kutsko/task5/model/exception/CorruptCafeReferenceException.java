package by.epam.javatraining.kutsko.task5.model.exception;

public class CorruptCafeReferenceException extends Exception {

	public CorruptCafeReferenceException() {
	}

	public CorruptCafeReferenceException(String message) {
		super(message);
	}

	public CorruptCafeReferenceException(Throwable cause) {
		super(cause);
	}

	public CorruptCafeReferenceException(String message, Throwable cause) {
		super(message, cause);
	}
}
