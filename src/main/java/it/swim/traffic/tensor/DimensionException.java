package it.swim.traffic.tensor;

public class DimensionException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public DimensionException(String message, Throwable cause) {
    super(message, cause);
  }

  public DimensionException(String message) {
    super(message);
  }

  public DimensionException(Throwable cause) {
    super(cause);
  }

  public DimensionException(int dimension) {
    super(Integer.toString(dimension));
  }

  public DimensionException() {
    super();
  }
}