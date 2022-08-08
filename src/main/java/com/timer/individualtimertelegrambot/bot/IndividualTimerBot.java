package com.timer.individualtimertelegrambot.bot;

import com.timer.individualtimertelegrambot.entity.Admin;
import com.timer.individualtimertelegrambot.entity.Restriction;
import com.timer.individualtimertelegrambot.repository.AdminRepository;
import com.timer.individualtimertelegrambot.repository.RestrictionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class IndividualTimerBot extends TelegramLongPollingBot {
    private RestrictionRepository restrictionRepository;
    private AdminRepository adminRepository;

    @Autowired
    public IndividualTimerBot(RestrictionRepository restrictionRepository, AdminRepository adminRepository) {
        super();
        this.restrictionRepository = restrictionRepository;
        this.adminRepository = adminRepository;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            String messageText = update.getMessage().getText();
            List<Restriction> restrictions = restrictionRepository.findAll();
            if(update.getMessage().getText() != null) {
                try {
                    if (Objects.equals(messageText, "/config")) {
                        config(update);
                    }
                    if (Objects.equals(messageText.split(" ")[0], "/mute")) {
                        if (isAdmin(update.getMessage().getFrom().getId(), update.getMessage().getChatId())) {
                            setTimer(update, restrictions);
                        } else {
                            sendMessage("You don't have permissions! Do you want to be muted?",
                                    update.getMessage().getChatId());
                        }
                    } else if (Objects.equals(messageText.split("@")[0], "/unmute")) {
                        if (isAdmin(update.getMessage().getFrom().getId(), update.getMessage().getChatId())) {
                            deleteTimer(update, restrictions);
                        } else {
                            sendMessage("You don't have permissions! Do you want to be muted?",
                                    update.getMessage().getChatId());
                        }
                    }
                } catch (Exception e) {
                    sendMessage("Message must be reply", update.getMessage().getChatId());
                }
            }
            checkRestrictions(update, restrictions);
        }
    }

    @Override
    public String getBotUsername() {
        return "IndividualTimerBot";
    }

    @Override
    public String getBotToken() {
        return "5010703825:AAFl9O4oBzt35G00gwWo5u4jOnKc4QCtYxg";
    }

    @PostConstruct
    public void init() throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        try {
            botsApi.registerBot(this);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private boolean isAdmin(long userId, long chatId) {
        List<Admin> admins = adminRepository.getAllByChatId(chatId);
        return admins.stream().anyMatch(admin -> admin.getUserId().equals(userId));

    }

    private void config(Update update) {
        List<Admin> admins = adminRepository.getAllByChatId(update.getMessage().getChatId());
        if(admins.isEmpty()) {
            adminRepository.save(Admin.builder().chatId(update.getMessage().getChatId())
                    .userId(update.getMessage().getFrom().getId()).build());
            sendMessage(update.getMessage().getFrom().getFirstName() + " is a new admin", update.getMessage().getChatId());
        } else if (!isAdmin(update.getMessage().getFrom().getId(), update.getMessage().getChatId())) {
            sendMessage("You don't have permissions, maybe you want mute?", update.getMessage().getChatId());
        }
    }

    private void checkRestrictions(Update update, List<Restriction> restrictions) {
        for (Restriction restriction : restrictions) {
            if (restriction.getChatId().equals(update.getMessage().getChatId())
                    && restriction.getUserId().equals(update.getMessage().getFrom().getId())) {
                if (restriction.getLastMessage() != null && ChronoUnit.SECONDS.between(
                        restriction.getLastMessage().toLocalDateTime(), LocalDateTime.now()) < restriction.getSecondsLimitation()) {
                    DeleteMessage delete = new DeleteMessage();
                    delete.setChatId(update.getMessage().getChatId());
                    delete.setMessageId(update.getMessage().getMessageId());
                    try {
                        execute(delete);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                } else {
                    restriction.setLastMessage(ZonedDateTime.now());
                    restrictionRepository.save(restriction);
                }
            }
        }
    }

    private Optional<Restriction> findRestriction(List<Restriction> restrictions, long userId, long chatId) {
        return restrictions.stream().filter(r -> r.getChatId().equals(chatId) && r.getUserId()
                .equals(userId)).findFirst();
    }

    private void deleteTimer(Update update, List<Restriction> restrictions) {
        Optional<Restriction> restriction = findRestriction(restrictions, update.getMessage().getReplyToMessage().getFrom().getId(),
                update.getMessage().getChatId());
        if(restriction.isPresent()) {
            restrictionRepository.delete(restriction.get());
        } else {
            sendMessage("User wasn't muted", update.getMessage().getChatId());
        }
    }

    private void setTimer(Update update, List<Restriction> restrictions) {
        long restrictedUserId = update.getMessage().getReplyToMessage().getFrom().getId();
        try {
            long secondsAmount = Long.parseLong(update.getMessage().getText().split(" ")[1]);
            Optional<Restriction> optionalRestriction = findRestriction(restrictions, restrictedUserId,
                    update.getMessage().getChatId());
            Restriction restriction;
            if (optionalRestriction.isEmpty()) {
                restriction = Restriction.builder().chatId(update.getMessage().getChatId()).userId(restrictedUserId)
                        .secondsLimitation(secondsAmount).build();
                restrictionRepository.save(restriction);
            } else {
                restriction = optionalRestriction.get();
                restriction.setSecondsLimitation(secondsAmount);
            }
            restrictionRepository.save(restriction);
        } catch (Exception e) {
            sendMessage("Enter amount of seconds!", update.getMessage().getChatId());
        }
    }

    private void sendMessage(String text, long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}