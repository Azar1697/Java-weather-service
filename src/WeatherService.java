import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;
import org.json.JSONArray;


public class WeatherService {
    // Api-ключ
    private static final String API_KEY = "57998fe7-0f98-4bbe-ba6d-dbfd7778eb44";
    private static final String BASE_URL = "https://api.weather.yandex.ru/v2/forecast";

    public static void main(String[] args) {
        // Координаты (широта и долгота) Города Темрюк Краснодарский край
        double lat = 45.16;
        double lon = 37.23;
        int limit = 7;

        try {
            // Получаем данные о погоде
            String jsonResponse = getWeatherData(lat, lon, limit);

            // Выводим полный JSON-ответ
            System.out.println("Полный ответ от API: " + jsonResponse);

            // Парсим и выводим текущую температуру
            double currentTemp = getCurrentTemperature(jsonResponse);
            System.out.println("Текущая температура: " + currentTemp + "°C");

            // Вычисляем среднюю температуру за указанный период
            double averageTemp = calculateAverageTemperature(jsonResponse, limit);
            System.out.println("Средняя температура за " + limit + " дней: " + averageTemp + "°C");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Метод для выполнения GET-запроса к API Яндекс Погоды
    private static String getWeatherData(double lat, double lon, int limit) throws Exception {
        String urlStr = BASE_URL + "?lat=" + lat + "&lon=" + lon + "&limit=" + limit;
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        conn.setRequestProperty("X-Yandex-API-Key", API_KEY);

        // Проверяем ответ на успешный статус 200
        int responseCode = conn.getResponseCode();
        if (responseCode == 403) {
            throw new Exception("Доступ запрещен: неверный API-ключ или отсутствует разрешение на доступ.");
        } else if (responseCode != 200) {
            throw new Exception("Ошибка: " + responseCode);
        }

        // Чтение ответа
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder response = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }


    // Получение текущей температуры из JSON-ответа
    private static double getCurrentTemperature(String jsonResponse) {
        JSONObject jsonObject = new JSONObject(jsonResponse);
        return jsonObject.getJSONObject("fact").getDouble("temp");
    }

    // Вычисление средней температуры за указанный период
    private static double calculateAverageTemperature(String jsonResponse, int limit) {
        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray forecasts = jsonObject.getJSONArray("forecasts");

        double sumTemp = 0;
        for (int i = 0; i < forecasts.length(); i++) {
            JSONObject forecast = forecasts.getJSONObject(i);
            sumTemp += forecast.getJSONObject("parts").getJSONObject("day").getDouble("temp_avg");
        }

        return sumTemp / forecasts.length();
    }
}
