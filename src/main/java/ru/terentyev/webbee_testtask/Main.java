package ru.terentyev.webbee_testtask;

import ru.terentyev.webbee_testtask.exceptions.DirectoryNotDefinedException;
import ru.terentyev.webbee_testtask.models.Operation;
import ru.terentyev.webbee_testtask.models.User;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class Main {
    private static String userLogsDir;
    private static final Map<User, TreeSet<Operation>> USERS_OPERATIONS = new HashMap<>();
    private static final Set<Operation> OPERATIONS = new TreeSet<>(Comparator.comparing(Operation::getPerformedAt));


    public static void main(String[] args) throws DirectoryNotDefinedException, IOException {

        if (args.length < 1 || args[0].isBlank()) {
            throw new DirectoryNotDefinedException();
        }

        Path inputRootDirPath = Paths.get(args[0]);
        if (!Files.exists(inputRootDirPath)) {
            throw new FileNotFoundException("File at path " + inputRootDirPath.toAbsolutePath() + " not found");
        }

        userLogsDir = inputRootDirPath.toAbsolutePath() + File.separator + "transactions_by_users";
        Path userLogDirPath = Paths.get(userLogsDir);


        deleteUserLogsIfExists(userLogDirPath);
        Files.createDirectory(userLogDirPath);
        readLogs(inputRootDirPath);
        OPERATIONS.forEach(Operation::perform);
        writeUserLogFiles();
    }

    private static void deleteUserLogsIfExists(Path userLogDirPath){
        if (Files.exists(userLogDirPath)) {
            try (Stream<Path> entries = Files.walk(userLogDirPath)) {
                entries.sorted(Comparator.reverseOrder()).forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private static void readLogs(Path inputRootDirPath){
        try (Stream<Path> files = Files.walk(inputRootDirPath)) {
            files.filter(file -> file.getFileName().toString().endsWith(".log")
                            && !file.getParent().getFileName().toString().equals("transactions_by_users")).forEach(p -> {
                try (FileReader fr = new FileReader(p.toFile());
                     BufferedReader br = new BufferedReader(fr)) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        line = line.replace("balance inquiry", "balance-inquiry");
                        String[] dateTimeAndOthersVarsArray = line.split("]");
                        LocalDateTime logDateTime = LocalDateTime.parse(dateTimeAndOthersVarsArray[0].replace("[", "")
                                , DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault()));
                        String[] othersVarsArray = dateTimeAndOthersVarsArray[1].trim().split(" ");
                        String username = othersVarsArray[0];
                        User user = User.findUserByName(username);
                        if (user == null) {
                            user = new User(username);
                        }

                        Operation.OperationType operationType = Operation.OperationType.getTypeByDescription(othersVarsArray[1]);
                        BigDecimal sum = BigDecimal.valueOf(Double.parseDouble(othersVarsArray[2]));
                        User recipient = null;
                        if (operationType == Operation.OperationType.TRANSFER) {
                             recipient = User.findUserByName(othersVarsArray[4]);
                             if (recipient == null) {
                                 recipient = new User(othersVarsArray[4]);
                             }
                        }
                        new Operation(user, operationType, logDateTime, sum, recipient);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeUserLogFiles(){
        USERS_OPERATIONS.forEach((user, operationSet) -> {
            StringBuilder resultStringForWriteInUserLog = new StringBuilder();
            AtomicReference<String> dateTimeString = new AtomicReference<>();
            operationSet.forEach(operation -> {
                dateTimeString.set(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(operation.getPerformedAt()));
                resultStringForWriteInUserLog.append("[").append(dateTimeString.get()).append("] ").append(operation.getUser().getName()).append(" ")
                        .append(operation.getType().getDescription().replace("balance-inquiry", "balance inquiry")).append(" ")
                        .append(operation.getSum().setScale(2, RoundingMode.HALF_UP));
                if (operation.getRecipient() != null) {
                    resultStringForWriteInUserLog.append(" to ").append(operation.getRecipient().getName());
                }
                resultStringForWriteInUserLog.append(System.lineSeparator());
            });
            resultStringForWriteInUserLog.append("[").append(dateTimeString.get()).append("] ").append(user.getName()).append(" final balance ")
                    .append(user.getBalance());
            Path newUserLog = Paths.get(userLogsDir + File.separator + user.getName() + ".log");

            try {
                Files.deleteIfExists(newUserLog);
                Files.createFile(newUserLog);
            } catch (IOException e) {
                e.printStackTrace();
            }

            try (FileWriter fw = new FileWriter(newUserLog.toFile())) {
                fw.append(resultStringForWriteInUserLog);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static String getUserLogsDir(){
        return userLogsDir;
    }

    public static void setUserLogsDir(String userLogsDir){
        Main.userLogsDir = userLogsDir;
    }

    public static Map<User, TreeSet<Operation>> getUsersOperation(){
        return USERS_OPERATIONS;
    }

    public static Set<Operation> getOperations(){
        return OPERATIONS;
    }
}