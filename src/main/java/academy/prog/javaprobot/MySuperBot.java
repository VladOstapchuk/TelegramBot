package academy.prog.javaprobot;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Component
public class MySuperBot extends TelegramLongPollingBot {

    private final static int USER_COUNT = 100;

    private final BotCredentials botCredentials;
    private final UserService userService;

    public MySuperBot(TelegramBotsApi telegramBotsApi,
                      BotCredentials botCredentials,
                      UserService userService) {
        super(botCredentials.getBotToken());

        this.botCredentials = botCredentials;
        this.userService = userService;

        try {
            telegramBotsApi.registerBot(this);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText())
            return;

        String text = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();

        Optional<User> userOpt = userService.findUserByChatId(chatId);
        User user;

        if (userOpt.isPresent()) {
            user = userOpt.get();
            sendMessage(chatId, text);

            //self-deletion by user

            if (text.toLowerCase().startsWith("delete")) {
                userService.deleteUser(user);
                return;
            }

            //setting additional admin/ syntaxis: newAdmin 380XXXXXXXX

            if (text.startsWith("newAdmin")) {
                String userToAdmin = text.substring(text.indexOf(' ') + 1);
                boolean result = setNewAdmin(userToAdmin);
                if (result) {
                    sendMessage(chatId, userToAdmin + " is a new admin.");
                    return;
                } else {
                    sendMessage(chatId, "Error!");
                }
            }
            //deleting from front-end

            if (text.startsWith("/admin_page") && isAdmin(user)) {
                    sendMessage(chatId,  "To administer users use this link: http://localhost:8080/");
                    return;
                }

            if (isAdminCommand(text)) {
                processAdminCommand(chatId, text);
                return;
            }

            // pattern State
            if (user.getState() == 1) {
                boolean valid = Utils.isValidUkrainianPhoneNumber(text);

                if (valid) {
                    user.setPhone(text);
                    sendMessage(chatId, "Thanks! What is your name?");
                    user.incrementState();
                } else {
                    sendMessage(chatId, "Wrong phone number! Try again.");
                }
            } else if (user.getState() == 2) {
                user.setName(text);
                sendMessage(chatId, "Thanks!");
                user.incrementState();
            }
            userService.updateUser(user);


        } else {
            System.out.println(text);

            String[] parts = text.split(" ");
            String password = (parts.length == 2) ? parts[1] : "";

            sendMessage(chatId, "Hello, I'm bot!");
            sendEmoji(chatId, "src/img/welcome.jpg");
            sendMessage(chatId, "What is your phone number (38XXYYYYYYY)?");

            user = new User();
            user.setAdmin(isValidPassword(password));
            user.setChatId(chatId);
            user.setState(1L);

            userService.saveUser(user);
        }
    }

    private boolean isValidPassword(String password) {
        return "SecretPassword".equals(password);
    }

    // /broadcast message cvdfgd
    private void processAdminCommand(long chatId, String text) {
        int idx = text.indexOf(' ');
        if (idx < 0) {
            sendMessage(chatId, "Wrong admin command!");
            return;
        }

        String message = text.substring(idx + 1);

        long usersCount = userService.getUsersCount();
        long pagesCount = (usersCount / USER_COUNT) +
                ((usersCount % USER_COUNT > 0) ? 1 : 0);

        for (int i = 0; i < pagesCount; i++) {
            List<User> users = userService.findAllUsers(
                    PageRequest.of(i, USER_COUNT)
            );
            users.forEach(user -> sendMessage(user.getChatId(), message));
        }
    }

    private boolean isAdminCommand(String text) {
        return text.startsWith("/broadcast ");
    }

    public boolean setNewAdmin(String phone) {
        boolean result = false;
        try {
            Optional<User> newAdminOp = userService.findUserByPhone(phone);
            if (newAdminOp != null) {
                result = true;
                User newAdmin = newAdminOp.get();
                newAdmin.setAdmin(true);
                userService.updateUser(newAdmin);

            }
        } catch (NoSuchElementException e) {
            System.out.println("Error! No user found");
        }
        return result;
    }

    private boolean isAdmin(User user) {
        return user.getAdmin();
    }

    //method to send pictures, photos, etc
    private void sendEmoji(long chatId, String emoji){
        try {
            SendPhoto sendPhoto = new SendPhoto();
            sendPhoto.setChatId(chatId);
            sendPhoto.setPhoto(new InputFile(new File(emoji)));
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    private void sendMessage(long chatId, String message) {
        var msg = new SendMessage();
        msg.setText(message);
        msg.setChatId(chatId);

        try {
            execute(msg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return botCredentials.getBotName();
    }
}
