package ru.sbt.utils.backup;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import ru.sbt.utils.backup.command.AbstractCommand;
import ru.sbt.utils.backup.command.CommandFactory;
import ru.sbt.utils.backup.configuration.DatabaseServerInfo;
import ru.sbt.utils.backup.configuration.XmlConfiguration;
import ru.sbt.utils.backup.util.Logger;

public class BackupUtilMain {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Не введена команда.");
            System.exit(-1);
            return;
        }

        XmlConfiguration configuration = null;
        try {
            InputStream is = new FileInputStream("./configuration.xml");
            configuration = XmlConfiguration.readConfiguration(is);
            System.out.println("Configuration read successfully.");
            if (args.length >= 2 && args[args.length-2].equals("-password") && args[args.length-1] != null) { // second from the end and last parameter
                for (DatabaseServerInfo info : configuration.getDatabaseServers()) {
                    info.setPassword(args[args.length-1]);
                }
            }
        } catch (Exception ex) {
            System.err.println("Configuration read error. " + ex.toString());
            System.exit(-1);  // jenkins считать ошибкой выполнения
            return;
        }

        AbstractCommand command = CommandFactory.getCommand(args[0], configuration);
        if (command == null) {
            System.err.println("Command not found!");
            System.exit(-1);  // jenkins считать ошибкой выполнения
            return;
        }

        // Инициализируем логгер
        try {
            Logger.init(command, configuration);
        } catch (Exception e) {
            System.err.println("Не удалось проинициализировать логгер: "+e.toString());
            e.printStackTrace();
            System.exit(-1);
            return;
        }

        try {
            // запуск команды
            command.execute(args);
        } catch (Throwable t) {
            Logger.getInstance().error("Во время работы команды произошли ошибки: " + t.toString() + ".");
            t.printStackTrace();
        }

        /**
         * Выводим результаты в лог и консоль
         */
        String result = "Работа команды завершена. Результаты работы: " + Logger.getInstance().getStaticticalResult();
        Logger.getInstance().info(result);
        Logger.getInstance().close();
        if (Logger.getInstance().getErrorCount() > 0) {
            System.err.println(result);
            System.exit(-1);
        } else {
            System.out.println(result);
        }
    }
}
