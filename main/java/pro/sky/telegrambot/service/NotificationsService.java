package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.model.Update;
import org.springframework.scheduling.annotation.Scheduled;

public interface NotificationsService {
    void process(Update update);

    void bringDatabaseRecords();
}
