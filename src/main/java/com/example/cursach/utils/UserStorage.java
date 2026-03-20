package com.example.cursach.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class UserStorage {

    private static final String FILE_PATH = "users.json";

    // 🧱 Чтение всех пользователей
    public static JSONArray readUsers() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return new JSONArray(); // если файла нет — вернуть пустой массив
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            StringBuilder jsonText = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonText.append(line);
            }
            return new JSONArray(jsonText.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONArray();
        }
    }

    // 💾 Добавление нового пользователя
    public static void addUser(String login, String password) {
        JSONArray users = readUsers();
        JSONObject newUser = new JSONObject();
        newUser.put("login", login);
        newUser.put("password", password);

        users.put(newUser);
        saveUsers(users);
    }

    // 🧠 Проверка логина и пароля
    public static boolean validateUser(String login, String password) {
        JSONArray users = readUsers();
        for (int i = 0; i < users.length(); i++) {
            JSONObject user = users.getJSONObject(i);
            if (user.getString("login").equals(login) &&
                    user.getString("password").equals(password)) {
                return true;
            }
        }
        return false;
    }

    // ⚙️ Сохранение в файл
    private static void saveUsers(JSONArray users) {
        try (FileWriter writer = new FileWriter(FILE_PATH, StandardCharsets.UTF_8)) {
            writer.write(users.toString(4)); // 4 = красивое форматирование
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
