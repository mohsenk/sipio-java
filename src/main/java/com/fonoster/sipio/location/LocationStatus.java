package com.fonoster.sipio.location;

public enum LocationStatus {
    OK(200, "Successful request"),
    NOT_FOUND(404, "Unable to find resource/s"),
    BAD_REQUEST(400,"Bad request."),
    NOT_SUPPORTED(405,"This operation is not supported by this implementation of the API. Code=0001"),
    INTERNAL_SERVER_ERROR(500, "Ups something when wrong with the server :(");

    private int code;
    private String message;

    LocationStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int code() {
        return code;
    }

    public String message() {
        return message;
    }
}

