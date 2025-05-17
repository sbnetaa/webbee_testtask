package ru.terentyev.webbee_testtask.exceptions;

import ru.terentyev.webbee_testtask.models.Operation;
import ru.terentyev.webbee_testtask.models.User;

import java.math.BigDecimal;

public class MoneyLackException extends Exception {

    public MoneyLackException(User user, Operation operation, BigDecimal sum) {
        super("User " + user.getName() + " used a greater amount (" + sum + ") rather then his balance ("
                + user.getBalance() + ") in operation with id " + operation.getId());
    }
}
