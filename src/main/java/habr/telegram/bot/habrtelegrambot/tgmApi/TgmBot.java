package habr.telegram.bot.habrtelegrambot.tgmApi;

import com.fasterxml.jackson.databind.ObjectMapper;
import habr.telegram.bot.habrtelegrambot.tgmApi.response.ResponseMessage;
import habr.telegram.bot.habrtelegrambot.tgmApi.response.ResponseUpdates;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TgmBot {

    private final static String TGM_URL_API = "https://api.telegram.org";

    private final OkHttpClient client;

    private final String url;

    private final ObjectMapper mapper = new ObjectMapper();

    public TgmBot(String botToken, OkHttpClient okHttpClient) {
        client = okHttpClient;
        url = TGM_URL_API + "/bot" + botToken;
    }

    private Call getCall(String urlMethod, Map<String, String> params) {
        HttpUrl.Builder httpUrlBuilder = Objects.requireNonNull(HttpUrl.parse(url + "/" + urlMethod)).newBuilder();
        for (Entry<String, String> entry : params.entrySet()) {
            httpUrlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
        }

        Request request = new Request.Builder()
                .url(httpUrlBuilder.build().toString())
                .get()
                .build();

        return client.newCall(request);
    }

    public ResponseUpdates getUpdates(int offest, int limit) {
        Call call = getCall("getUpdates",
                Map.of("offset", String.valueOf(offest), "limit", String.valueOf(limit)));

        ResponseUpdates result = null;

        try (Response response = call.execute()) {
            result = mapper.readValue(Objects.requireNonNull(response.body()).string(), ResponseUpdates.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public ResponseMessage sendMessage(int chatId, String text) {
        Call call = getCall("sendMessage",
                Map.of("chat_id", String.valueOf(chatId), "text", text));

        ResponseMessage result = null;

        try (Response response = call.execute()) {
            result = mapper.readValue(Objects.requireNonNull(response.body()).string(), ResponseMessage.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }
}
