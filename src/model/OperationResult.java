package model;

public record OperationResult(String filename, Transaction transaction, model.OperationResult.Status status,
                              String message) {
    public enum Status {
        SUCCESS,
        ERROR
    }

}
