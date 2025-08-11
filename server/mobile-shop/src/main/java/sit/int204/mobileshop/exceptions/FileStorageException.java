package sit.int204.mobileshop.exceptions;

public class FileStorageException extends RuntimeException {
  public FileStorageException(String message) {
    super(message);
  }
  public FileStorageException(String message, Throwable cause) {
    super(message, cause);
  }
}
