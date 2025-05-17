package ru.terentyev.webbee_testtask.models;

import ru.terentyev.webbee_testtask.Main;
import ru.terentyev.webbee_testtask.exceptions.MoneyLackException;
import ru.terentyev.webbee_testtask.exceptions.NegativeSumException;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Objects;
import java.util.TreeSet;


public class User extends AbstractModel {

    private final String name;
    private BigDecimal balance;

    public User(String name) {
        this.name = name;
        Main.getUsersOperation().put(this, new TreeSet<>(Comparator.comparing(Operation::getPerformedAt)));
    }

    public static User findUserByName(String name) {
        for (User user : Main.getUsersOperation().keySet()) {
            if (user.getName().equals(name)) {
                return user;
            }
        }
        return null;
    }

    private void validateNegativeSum(BigDecimal sum, Operation operation) throws NegativeSumException {
        if (BigDecimal.ZERO.compareTo(sum) > 0) {
            operation.setFailed(true);
            throw new NegativeSumException(this, operation, sum);
        }
    }

    private void validateMoneyLack(BigDecimal sum, Operation operation) throws MoneyLackException {
        if (sum.compareTo(balance) > 0) {
            operation.setFailed(true);
            throw new MoneyLackException(this, operation, sum);
        }
    }

    public synchronized void balanceInquiry(BigDecimal sum){
        balance = sum;
    }

    public synchronized void deposit(BigDecimal sum, Operation operation) throws NegativeSumException {
        validateNegativeSum(sum, operation);
        balance = balance.add(sum);
    }

    public synchronized void withdraw(BigDecimal sum, Operation operation) throws MoneyLackException, NegativeSumException {
        validateNegativeSum(sum, operation);
        validateMoneyLack(sum, operation);
        balance = balance.subtract(sum);
    }

    public synchronized void transfer(User recipient, BigDecimal sum, Operation operation) throws NegativeSumException, MoneyLackException {
        if (balance != null) {
            validateNegativeSum(sum, operation);
            validateMoneyLack(sum, operation);
            balance = balance.subtract(sum);
        }
        if (recipient.getBalance() != null) {
            recipient.setBalance(recipient.getBalance().add(sum));
        }

    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(name, user.name) && Objects.equals(balance, user.balance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, balance);
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getName() {
        return name;
    }
}
