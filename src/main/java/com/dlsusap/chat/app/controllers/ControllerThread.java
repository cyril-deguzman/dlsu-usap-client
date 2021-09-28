package com.dlsusap.chat.app.controllers;

import com.dlsusap.chat.app.model.Message;

import java.io.*;

public class ControllerThread extends Thread{
    private Controller controller;
    private Message msg;
    public boolean reconBtn;

    public ControllerThread(Controller controller) {
        this.controller = controller;
    }

    @Override
    public void run() {
        try {
            reconBtn = true;
            ObjectOutputStream os = controller.os;
            ObjectInputStream is = controller.is;

            while((msg = (Message)is.readObject()) != null)
                switch(msg.text) {
                    case "RequestID":
                        Message toSend = new Message();
                        toSend.setSenderID(controller.id);
                        toSend.setSenderName(controller.nickName);
                        toSend.setText("ID");
                        os.writeObject(toSend);
                        break;
                    case "img":
                        controller.image = msg.getImage();
                        if(controller.image == null)
                            System.out.println("Null");
                        else
                            controller.showImage();
                        break;
                    case "file":
                        Message notice = new Message();
                        notice.setText("Click receive button to receive file (" + msg.getFileName() + ")\n");
                        notice.setSenderName("[Server]");
                        controller.msg = notice;
                        controller.receiveMessage();
                        controller.msg = msg;
                        break;
                    case "fileInfo":
                        Message fileInfo = new Message();
                        fileInfo.setText(msg.getSenderName() + " uploaded (" + msg.getFileName()+")\n");
                        fileInfo.setSenderName("[Server]");
                        controller.msg = fileInfo;
                        controller.receiveMessage();
                        controller.updateFileArea(msg.getFileName());
                        break;
                    default:
                        controller.msg = msg;
                        controller.receiveMessage();
                }

        } catch(Exception e) {
            controller.initRecon(reconBtn);
            e.printStackTrace();
        }


    }
}
