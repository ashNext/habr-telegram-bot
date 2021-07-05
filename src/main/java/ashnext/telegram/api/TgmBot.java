package ashnext.telegram.api;

import ashnext.telegram.api.response.ResponseBoolean;
import ashnext.telegram.api.response.ResponseMessage;
import ashnext.telegram.api.response.ResponseUpdates;
import ashnext.telegram.api.response.TgmResponse;
import ashnext.telegram.api.types.BotCommand;
import ashnext.telegram.api.types.InlineKeyboardMarkup;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

@Slf4j
public class TgmBot {

    private final static String TGM_URL_API = "https://api.telegram.org";

    private final static List<BotCommand> COMMANDS_LIST = List.of(
            new BotCommand("/sub", "Subscribe to articles"),
            new BotCommand("/unsub", "Cancel subscription")
    );

    private final OkHttpClient client;

    private final String url;

    private final ObjectMapper mapper = new ObjectMapper();

    public TgmBot(String botToken, OkHttpClient okHttpClient) {
        client = okHttpClient;
        url = TGM_URL_API + "/bot" + botToken;
        setMyCommands(COMMANDS_LIST);
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

    public Optional<ResponseMessage> sendMessage(int chatId, String text, InlineKeyboardMarkup replyMarkup) {
        try {
            Call call = getCall("sendMessage",
                    Map.of("chat_id", String.valueOf(chatId),
                            "text", text,
                            "reply_markup", mapper.writeValueAsString(replyMarkup)));
            return getResponse(call, ResponseMessage.class);
        } catch (JsonProcessingException e) {
            log.error("Invalid write to JSON", e);
        }
        return Optional.empty();
    }

    public Optional<ResponseBoolean> deleteMessage(int chatId, int messageId) {
        Call call = getCall("deleteMessage",
                Map.of("chat_id", String.valueOf(chatId), "message_id", String.valueOf(messageId)));
        return getResponse(call, ResponseBoolean.class);
    }

    private Optional<ResponseBoolean> setMyCommands(List<BotCommand> commands) {
        try {
            Call call = getCall("setMyCommands",
                    Map.of("commands", mapper.writeValueAsString(commands)));
            return getResponse(call, ResponseBoolean.class);
        } catch (JsonProcessingException e) {
            log.error("Invalid write to JSON", e);
        }

        return Optional.empty();
    }
}