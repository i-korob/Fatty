package enot;

import java.util.ArrayList;
import java.util.List;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;

public class telega extends TelegramLongPollingBot {

	public static void main(String[] args) {
		ApiContextInitializer.init();
		TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
		try {
			telegramBotsApi.registerBot(new telega());
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getBotUsername() {
		return "BotName";
	}

	@Override
	public String getBotToken() {
		return "BotToken";
	}

	@Override
		public void onUpdateReceived(Update update) {
			Message message = update.getMessage();
			if (message != null && message.hasText()) {
				long startTime1 = System.currentTimeMillis();
				int answer = 0;
				if (message.getText().equals("/help")){
					answer = readtxt.readFile("08412219.txt");
					sendMsg(message, "Привет, я робот");
					}
				else
					startTime1 = System.currentTimeMillis();
					answer = memory.IncomigMessage(String.valueOf(message.getText()));
					long estimatedTime = System.currentTimeMillis()-startTime1;
					sendMsg(message, String.valueOf("Всего индексов: "+ String.valueOf(answer))+ System.lineSeparator() + "Время: "+String.valueOf(estimatedTime)+" миллис");
			}
		}

	private void sendMsg(Message message, String text) {
		SendMessage sendMessage = new SendMessage();
		sendMessage.enableMarkdown(true);
		sendMessage.setChatId(message.getChatId().toString());
		sendMessage.setReplyToMessageId(message.getMessageId());
		sendMessage.setText(text);
		try {
			sendMessage(sendMessage);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}
}
