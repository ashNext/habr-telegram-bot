package habr.telegram.bot.habrtelegrambot.tgmApi;

import com.fasterxml.jackson.databind.ObjectMapper;
import habr.telegram.bot.habrtelegrambot.tgmApi.exception.TgmResponseException;
import habr.telegram.bot.habrtelegrambot.tgmApi.response.ResponseMessage;
import habr.telegram.bot.habrtelegrambot.tgmApi.response.ResponseUpdates;
import habr.telegram.bot.habrtelegrambot.tgmApi.response.TgmResponse;
import okhttp3.*;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

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

    private <T extends TgmResponse> T readJsonValue(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid read from JSON:\n'" + json + "'", e);
        }
    }

    private <T extends TgmResponse> T getResponse(Call call, Class<T> clazz) {
        try (Response response = call.execute()) {
            T result = readJsonValue(Objects.requireNonNull(response.body()).string(), clazz);
            if (!result.isOk()) {
                throw new TgmResponseException(result);
            }
            return result;
        } catch (IOException e) {
            throw new IllegalCallerException("Invalid call: " + e);
        }
    }

    public ResponseUpdates getUpdates(int offset, int limit) {
        Call call = getCall("getUpdates",
                Map.of("offset", String.valueOf(offset), "limit", String.valueOf(limit)));

        return getResponse(call, ResponseUpdates.class);
    }

    public ResponseMessage sendMessage(int chatId, String text) {
        Call call = getCall("sendMessage",
                Map.of("chat_id", String.valueOf(chatId), "text", text));

        return getResponse(call, ResponseMessage.class);
    }
}
