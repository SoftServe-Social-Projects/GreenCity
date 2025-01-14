package greencity.exception.exceptions;

/**
 * Exception that we get when user trying to add a place that already exists in
 * the database.
 *
 * @author Hrenevych Ivan
 */
public class PlaceAlreadyExistsException extends RuntimeException {
    /**
     * Constructor for PlaceAlreadyExistsException.
     *
     * @param message - giving message.
     */
    public PlaceAlreadyExistsException(String message) {
        super(message);
    }
}