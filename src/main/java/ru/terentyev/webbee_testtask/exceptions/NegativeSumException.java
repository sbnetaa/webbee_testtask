package ru.terentyev.webbee_testtask.exceptions;

import ru.terentyev.webbee_testtask.models.Operation;
import ru.terentyev.webbee_testtask.models.User;

import java.math.BigDecimal;

public class NegativeSumException extends Exception {

    public NegativeSumException(User user, Operation operation, BigDecimal sum) {
        super("User " + user.getName() + " used negative sum (" + sum + ") in operation with id " + operation.getId());
    }
}
