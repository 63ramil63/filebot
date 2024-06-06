package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;

import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.File;

import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


import java.io.*;

import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.List;



public class bot extends TelegramLongPollingBot {
    String[] form = {"png", "jpg", "gif", "bmp", "jpeg" ,"pdf", "txt", "rtf", "docs", "doc", "wps"};
    Path folderPath = Paths.get("F://Files/");

    @Override
    public void onUpdateReceived(Update update){
        boolean canDownload = false;
        String message = update.getMessage().getText();
        SendMessage sendMessage = new SendMessage();
        String chatId = update.getMessage().getChatId().toString();
        sendMessage.setChatId(chatId);



        if(update.hasMessage()&&update.getMessage().hasDocument()&&update.hasMessage()){
            String name = update.getMessage().getCaption();//Описание файла
            System.out.println(name + " name");
            Document document = update.getMessage().getDocument();//получение файла
            String fileid = document.getFileId();//получение имени файла
            GetFile getFile = new GetFile();//получение файла от бота
            getFile.setFileId(fileid);
            String filename = document.getFileName();
            String fileExtension = filename.substring(filename.lastIndexOf("."));//оставляем расширение файла

            for(String word: form){//проверка файла на расширение
                if(filename.contains(word)){
                    canDownload = true;
                    break;
                }
            }
            if (canDownload) {
                try {
                    File file = execute(getFile);
                    InputStream is = new URL("https://api.telegram.org/file/bot" + getBotToken() + "/" + file.getFilePath()).openStream();//открытие потока для чтения файла
                    if(name == null){
                        Files.copy(is, Paths.get("F:/Files/" + filename));//копирование файла на компьютер

                    }else{

                        Files.copy(is, Paths.get("F:/Files/" + name + fileExtension));//копирование файла на компьютер
                    }
                    is.close();
                    sendMessage.setText("Сохранено");
                    execute(sendMessage);
                } catch (TelegramApiException | IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if(message.equals("/start")){
            sendMessage.setText("Добро пожаловать на всратого бота\n" +
                    "Чтобы посмотреть файлы нажмите  кнопку обновить\n" +
                    "Чтобы добавить файлы выберите отправьте их боту(не отправляйте ненужный хлам(фотки с Каримычем не считаются хламом))\n" +
                    "Указывайте название файла так, чтобы другие поняли для чего он\n" +
                    "Если указываете caption* то файл сохраниться с таким названием");
            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }





        ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove();//удаление старой клавиатуры
        replyKeyboardRemove.setRemoveKeyboard(true);
        sendMessage.setReplyMarkup(replyKeyboardRemove);

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();//создание  новой
        List<KeyboardRow> keyboard = new ArrayList<>();

        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        KeyboardRow keyboardRow1 = new KeyboardRow();
        keyboardRow1.add(new KeyboardButton("Обновить"));
        keyboard.add(keyboardRow1);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setKeyboard(keyboard);

        try(DirectoryStream<Path> stream = Files.newDirectoryStream(folderPath)){
            for(Path path: stream){
                String pathName = path.toString();
                String deleteText = "F:\\Files\\";
                String buttonName = pathName.replace(deleteText, "");//удаление пути для надписи
                KeyboardRow keyboardRow = new KeyboardRow();
                keyboardRow.add(new KeyboardButton(buttonName));
                keyboard.add(keyboardRow);
                sendMessage.setReplyMarkup(replyKeyboardMarkup);
            }
        }catch (IOException e){
            e.printStackTrace();
        }



        sendMessage.setText("Ща");//нужно для обновления клавиатуры
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }


        if(message.contains(".")) {
            SendDocument sendDocument = new SendDocument();
            sendDocument.setChatId(chatId);
            sendDocument.setDocument(new InputFile(new java.io.File(folderPath + "\\" + message)));
            sendDocument.setReplyMarkup(replyKeyboardMarkup);
            try {
                execute(sendDocument);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }

    }







    @Override
    public String getBotToken(){
        return "";
    }

    @Override
    public String getBotUsername(){
        return "";
    }


}
