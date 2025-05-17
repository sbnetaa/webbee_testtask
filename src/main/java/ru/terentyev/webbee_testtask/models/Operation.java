package ru.terentyev.webbee_testtask.models;

import ru.terentyev.webbee_testtask.Main;
import ru.terentyev.webbee_testtask.exceptions.MoneyLackException;
import ru.terentyev.webbee_testtask.exceptions.NegativeSumException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class Operation extends AbstractModel {

    /**
     * Владелец операции
     */
    private final User user;

    /**
     * Возможные типы операции:
     * balance inquiry - запрос баланса
     * transferred - перевод на чужой счет
     * withdrew - обналичивание
     */
    private final OperationType type;

    /**
     * Дата и время выполнения операции
     */
    private final LocalDateTime performedAt;

    /**
     * Сумма для перевода/обналичивания,
     * либо ее размер на момент проверки баланса
     */
    private BigDecimal sum;

    /**
     * Получатель перевода
     */
    private User recipient;

    /**
     * Была ли операция прервана
     */
    private boolean failed;

    public Operation(User user, OperationType type, LocalDateTime performedAt, BigDecimal sum, User recipient) {
        this.user = user;
        this.type = type;
        this.performedAt = performedAt;
        this.sum = sum;
        this.recipient = recipient;
        Main.getUsersOperation().get(user).add(this);
        Main.getOperations().add(this);
    }

    public void perform(){
        try {
            if (type == OperationType.BALANCE_INQUIRY) {
                user.balanceInquiry(sum);
            } else if (type == OperationType.TRANSFER) {
                user.transfer(recipient, sum, this);
            }
            if (user.getBalance() == null) {
                return;
            }
            if (type == OperationType.DEPOSIT) {
                user.deposit(sum, this);
            } else if (type == OperationType.WITHDRAW) {
                user.withdraw(sum, this);
            }
        } catch (NegativeSumException | MoneyLackException e) {
            e.printStackTrace();
        }

    }

    public User getUser() {
        return user;
    }

    public OperationType getType() {
        return type;
    }

    public LocalDateTime getPerformedAt() {
        return performedAt;
    }

    public BigDecimal getSum() {
        return sum;
    }

    public void setSum(BigDecimal sum) {
        this.sum = sum;
    }

    public User getRecipient() {
        return recipient;
    }

    public void setRecipient(User recipient) {
        this.recipient = recipient;
    }

    public boolean isFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Operation operation = (Operation) o;
        return failed == operation.failed && Objects.equals(user, operation.user) && type == operation.type && Objects.equals(performedAt, operation.performedAt) && Objects.equals(sum, operation.sum) && Objects.equals(recipient, operation.recipient);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, type, performedAt, sum, recipient, failed);
    }

    public enum OperationType {
        BALANCE_INQUIRY("balance-inquiry"),
        DEPOSIT("deposit"),
        WITHDRAW("withdrew"),
        TRANSFER("transferred");

        private final String description;

        OperationType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public static OperationType getTypeByDescription(String description){
            for (OperationType type : OperationType.values()) {
                if (type.getDescription().equals(description)) {
                    return type;
                }
            }
            return null;
        }
    }
}
