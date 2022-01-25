package com.example.linebot.handler;

import com.example.linebot.util.LineUtil;
import com.linecorp.bot.model.action.Action;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.PostbackEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.template.ButtonsTemplate;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@LineMessageHandler
public class LineHandler {

    private final LineUtil lineUtil;

    private static final String TOP_MENU_WRITE = "家計簿をつける";
    private static final String TOP_MENU_READ = "家計簿をみる";
    private static final TemplateMessage TOP_MENU = new TemplateMessage(
            "メニュー",
            new ButtonsTemplate(null, null, "メニューを選択してください", convertToPostbackAction(
                    TOP_MENU_WRITE, TOP_MENU_READ)));

    private static final List<String> EXPENSE_ITEMS = List.of("食費", "車");
    private static final TemplateMessage EXPENSE_ITEMS_MENU = new TemplateMessage(
            "費目",
            new ButtonsTemplate(null, null, "費目を選択してください", convertToPostbackAction(EXPENSE_ITEMS)));

    // TODO DBやRedis等に保存する
    private final Map<String, String> userAndSelectedItem = new HashMap<>();


    @EventMapping
    public void handleTextMessageEvent(MessageEvent<TextMessageContent> event) {
        String userId = event.getSource().getUserId();
        String selectedItem = userAndSelectedItem.get(userId);
        if (selectedItem != null && EXPENSE_ITEMS.contains(selectedItem)) {
            TextMessageContent message = event.getMessage();
            String text = message.getText();
            if (text.matches("[1-9][0-9]*")) {
                int amount = Integer.parseInt(text);
                // TODO DB保存
                lineUtil.replyText(event, selectedItem + "を" + amount + "円で登録しました");
                userAndSelectedItem.remove(userId);
            } else {
                lineUtil.replyText(event, "数字のみで金額を入力してください");
            }
        } else {
            lineUtil.reply(event, TOP_MENU);
        }
    }

    @EventMapping
    public void handlePostbackEvent(PostbackEvent event) {
        String data = event.getPostbackContent().getData();
        switch (data) {
            case TOP_MENU_WRITE -> lineUtil.reply(event, EXPENSE_ITEMS_MENU);
            case TOP_MENU_READ -> lineUtil.replyText(event, "未実装です");
            default -> {
                userAndSelectedItem.put(event.getSource().getUserId(), data);
                lineUtil.replyText(event, data + "の金額を数字のみで入力してください");
            }
        }
    }

    private static List<Action> convertToPostbackAction(List<String> list) {
        return list.stream().map(x -> new PostbackAction(x, x)).collect(Collectors.toList());
    }

    private static List<Action> convertToPostbackAction(String... list) {
        return convertToPostbackAction(List.of(list));
    }
}
