package com.example.linebot.util;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.ReplyEvent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.response.BotApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RequiredArgsConstructor
@Component
public class LineUtil {

    private final LineMessagingClient lineMessagingClient;

    public BotApiResponse replyText(ReplyEvent event, String message) {
        if (message.length() > 1000) {
            message = message.substring(0, 1000 - 2) + "……";
        }
        return reply(event, new TextMessage(message));
    }

    public BotApiResponse reply(ReplyEvent event, Message message) {
        return reply(event, List.of(message));
    }

    public BotApiResponse reply(ReplyEvent event, List<Message> messages) {
        return reply(event, messages, false);
    }

    public BotApiResponse reply(ReplyEvent event, List<Message> messages, boolean notificationDisabled) {
        try {
            return lineMessagingClient
                    .replyMessage(new ReplyMessage(event.getReplyToken(), messages, notificationDisabled))
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
