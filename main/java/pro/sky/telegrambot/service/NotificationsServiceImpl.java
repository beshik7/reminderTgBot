package pro.sky.telegrambot.service;


import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.model.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.Notification;
import pro.sky.telegrambot.repository.NotificationsRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NotificationsServiceImpl implements NotificationsService {

    private final Logger logger = LoggerFactory.getLogger(NotificationsServiceImpl.class);
    private final TelegramBot telegramBot;
    // Паттерн для проверки корректности формата входящего сообщения.
    private static final Pattern MESSAGE_PATTERN = Pattern.compile("([0-9\\.\\:\\s]{16})(\\s)(.+)");
    // Форматтер для конвертации строки в LocalDateTime.
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final NotificationsRepository repository;

    public NotificationsServiceImpl(TelegramBot telegramBot,  NotificationsRepository repository) {
        this.telegramBot = telegramBot;
        this.repository = repository;
    }

    @Override
    public void process(Update update) {
        // Проверяем наличие текста в сообщении.
        if (update.message() == null || update.message().text() == null) {
            logger.info("You sent an empty message.");
            return;
        }

        Long chatId = update.message().chat().id();
        String customerMessage = update.message().text();

        // Проверяем, что сообщение не пустое.
        if (customerMessage == null) {
            telegramBot.execute(new SendMessage(chatId, "Для начала работы с ботом, отправь /start"));
            return;
        }


        // Обрабатываем команду /start.
        if (customerMessage.equals("/start")) {
            welcomeMessageSent(chatId);
            return;
        }

        Matcher matcher = MESSAGE_PATTERN.matcher(customerMessage);

        // Проверяем формат сообщения.
        if (!matcher.matches()) {
            telegramBot.execute(new SendMessage(chatId, "Добавить напоминание можно только в формате: 'dd.MM.yyyy HH:mm текст напоминания'"));
            return;
        }

        // Валидация формата даты.
        formatOfDateValidation(chatId, matcher);

        LocalDateTime reminderDate = LocalDateTime.parse(matcher.group(1), DATE_TIME_FORMATTER);
        String notification = matcher.group(2);

        saveEntity(chatId, notification, reminderDate, LocalDateTime.now());
    }
    //приветственное сообщение
    private void welcomeMessageSent(long chatId) {
        telegramBot.execute(new SendMessage(chatId, "Приветствую тебя! Я здесь, чтобы помочь с навигацией. Я принимаю напоминание в формате: 'dd.MM.yyyy HH:mm текст напоминания'"));
    }
    /**
     * Проверяет корректность формата и валидность даты, предоставленной пользователем.
     * Дата должна соответствовать формату 'dd.MM.yyyy HH:mm' и быть в будущем.
     * В случае неверного формата или даты из прошлого, отправляет соответствующее сообщение пользователю через Telegram Bot.
     *
     * @param chatId идентификатор чата с пользователем.
     * @param matcher объект Matcher, содержащий результаты разбора входящего сообщения.
     */
    private void formatOfDateValidation(long chatId, Matcher matcher) {
        String dateString = matcher.group(1);
        try {
            LocalDateTime reminderDate = LocalDateTime.parse(dateString, DATE_TIME_FORMATTER);
            if (!reminderDate.isAfter(LocalDateTime.now())) {
                telegramBot.execute(new SendMessage(chatId, "К сожалению, даты из прошлого указывать нельзя!"));
                logger.warn("The provided date is in the past");
            }
        } catch (DateTimeParseException e) {
            telegramBot.execute(new SendMessage(chatId, "Исключение! Мы поддерживаем только формат: 'dd.MM.yyyy HH:mm текст напоминания'"));
            logger.info("Use correct date format ");
        }


    }

    private void saveEntity(Long chatId, String notificationContent, LocalDateTime reminderDate, LocalDateTime timeAdded) {
        // Создаем новую сущность напоминания и сохраняем в базу данных.
        Notification notification = new Notification(chatId, notificationContent, reminderDate, timeAdded);
        repository.save(notification);
        telegramBot.execute(new SendMessage(chatId, "Я запомнил! " + notification));
        logger.info("Notification saved: " + notification);

    }


    @Override
    @Scheduled(cron = "0 0/1 * * * *")
    public void bringDatabaseRecords() {
        List<Notification> records = repository.findByReminderDate(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));

        // Для каждой записи из базы данных отправляем напоминание.
        records.forEach(record -> {
            logger.info("Notification sent successfully.");
            telegramBot.execute(new SendMessage(record.getChatId(), String.format("Привет! Обязательно вспомни: \n%s в %s", record.getNotification(), record.getReminderDate())));
        });
    }
}

