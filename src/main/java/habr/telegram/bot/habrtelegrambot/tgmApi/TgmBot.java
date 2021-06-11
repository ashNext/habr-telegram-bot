package habr.telegram.bot.habrtelegrambot.tgmApi;

import com.fasterxml.jackson.databind.ObjectMapper;
import habr.telegram.bot.habrtelegrambot.tgmApi.response.ResponseMessage;
import habr.telegram.bot.habrtelegrambot.tgmApi.response.ResponseUpdates;
import habr.telegram.bot.habrtelegrambot.tgmApi.response.TgmResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TgmBot {

    private final static String TGM_URL_API = "https://api.telegram.org";

    private final OkHttpClient client;

    private final String url;

    private final ObjectMapper mapper = new ObjectMapper();

    private final Logger log = LoggerFactory.getLogger(getClass());

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

    private <T extends TgmResponse> Optional<T> readJsonValue(String json, Class<T> clazz) {
        try {
            return Optional.of(mapper.readValue(json, clazz));
        } catch (IOException e) {
            log.error("Invalid read from JSON:\n'" + json + "'", e);
            return Optional.empty();
        }
    }

    private <T extends TgmResponse> Optional<T> getResponse(Call call, Class<T> clazz) {
        try (Response response = call.execute()) {
            Optional<T> optResponse = readJsonValue(Objects.requireNonNull(response.body()).string(), clazz);

            if (optResponse.isPresent()) {
                if (optResponse.get().isOk()) {
                    return optResponse;
                } else {
                    log.error("Error getting response by url={}:\n error_code={}, description={}",
                            call.request().url(),
                            optResponse.get().getErrorCode(),
                            optResponse.get().getDescription());
                    return Optional.empty();
                }
            } else {
                return Optional.empty();
            }
        } catch (IOException e) {
            log.error("Invalid call: ", e);
            return Optional.empty();
        }
    }

    public Optional<ResponseUpdates> getUpdates(int offset, int limit) {
        Call call = getCall("getUpdates",
                Map.of("offset", String.valueOf(offset), "limit", String.valueOf(limit)));

        return getResponse(call, ResponseUpdates.class);
    }

    public Optional<ResponseMessage> sendMessage(int chatId, String text) {
        Call call = getCall("sendMessage",
                Map.of("chat_id", String.valueOf(chatId), "text", text));

        return getResponse(call, ResponseMessage.class);
    }
}
