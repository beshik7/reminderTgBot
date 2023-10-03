package pro.sky.telegrambot.service;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.sky.telegrambot.repository.NotificationsRepository;

import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
public class NotificationsServiceImplTest {

    @Mock
    private TelegramBot telegramBot;

    @Mock
    private NotificationsRepository repository;

    @InjectMocks
    private NotificationsServiceImpl service;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }
    @Test
    public void testProcessWithStartCommand() {
        Update mockUpdate = createMockUpdate("/start");

        service.process(mockUpdate);

        verify(telegramBot, times(1)).execute(any(SendMessage.class));
    }


    private Update createMockUpdate(String text) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);

        when(update.message()).thenReturn(message);
        when(message.text()).thenReturn(text);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(12345L);

        return update;
    }


}