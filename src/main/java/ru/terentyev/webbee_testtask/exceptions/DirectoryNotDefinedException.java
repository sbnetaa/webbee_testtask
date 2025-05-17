package ru.terentyev.webbee_testtask.exceptions;

public class DirectoryNotDefinedException extends Exception {

    public DirectoryNotDefinedException() {
        super("You need to define base input- and output-logs path using 'args' parameter when starting the application." +
                " Input dir must be the first arg.");
    }
}
