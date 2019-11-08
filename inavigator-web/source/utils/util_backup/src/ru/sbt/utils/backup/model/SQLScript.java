package ru.sbt.utils.backup.model;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SQLScript {
    private final String name;
    private final String sql;

    public SQLScript(String name, String sql) {
        this.name = name;
        this.sql = sql;
    }

    public String getName() {
        return name;
    }
    public String getSql() {
        return sql;
    }

    public static List<SQLScript> getScriptsFromFile(File file) throws IOException {
        String name = file.getName();
        List<SQLScript> result = new ArrayList<SQLScript>();
        List<String> lines = new ArrayList<String>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")));
            try {
                String line = reader.readLine();
                while (line != null) {
                    lines.add(line);
                    line = reader.readLine();
                }
            } finally {
                reader.close();
            }
        } catch (MalformedInputException e) {
            throw new IOException("Файл "+file.getName()+" содержит неизвестный символ (не в кодировке UTF-8)", e);
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (Iterator<String> iterator = lines.iterator(); iterator.hasNext(); ) {
            String next =  iterator.next();
            if (next.trim().startsWith("--"))
                continue;
            if ("go".equalsIgnoreCase(next.trim())) {
                String sql = stringBuilder.toString();
                if (!sql.trim().isEmpty()) {
                    SQLScript script = new SQLScript(name, sql);
                    result.add(script);
                }
                stringBuilder = new StringBuilder();
            } else {
                stringBuilder.append(next).append("\n");
            }
        }
        String sql = stringBuilder.toString();
        if (!sql.trim().isEmpty()) {
            SQLScript script = new SQLScript(name, sql);
            result.add(script);
        }
        return result;
    }

    @Override
    public String toString() {
        return name;
    }
}
