package com.dlsusap.chat.app.controllers;

import com.dlsusap.chat.app.model.Message;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.stage.*;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.Arrays;

public class Controller {

    /*Attributes*/
    private Stage stage;
    private String serverAddress;
    private int port;

    public Socket clientEndpoint;
    public ObjectOutputStream os;
    public ObjectInputStream is;
    public ControllerThread controllerThread;

    public String nickName;
    public int id;

    public Image image;
    public Message msg;

    /* FXML Attributes */
    @FXML
    private Button connectButton;
    @FXML
    private TextField portField;
    @FXML
    private Label statusLabel;
    @FXML
    private TextField addressField;
    @FXML
    private TextField useridField;
    @FXML
    private TextField usernameField;
    @FXML
    private Label loginLabel;
    @FXML
    private Button testButton;
    @FXML
    private ImageView imageBox;
    @FXML
    private TextArea chatBox;
    @FXML
    private Label appLabel;
    @FXML
    private TextField chatEntry;
    @FXML
    private Button sendBtn;
    @FXML
    private Button reconBtn;
    @FXML
    private Button logOutBtn;
    @FXML
    private Label userLabel;
    @FXML
    private Label idLabel;
    @FXML
    private Button sendFileBtn;
    @FXML
    private TextArea fileArea;
    @FXML
    private Label fileLabel;
    @FXML
    private Label pictureLabel;
    @FXML
    private TextField fileField;
    @FXML
    private Button downloadBtn;
    @FXML
    private Button getBtn;

    /* FXML ActionEvents */
    @FXML
    void connectToServer(ActionEvent event) {
        try {
            serverAddress = addressField.getText();
            port = Integer.parseInt(portField.getText());
            nickName = usernameField.getText();
            id = Integer.parseInt(useridField.getText());

            initConn();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void sendPic(ActionEvent event) {
        try {
            /* File Chooser and Filters*/
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose an Image");
            fileChooser.getExtensionFilters().
                    add(new FileChooser.ExtensionFilter
                            ("Image Files", "*.png", "*.jpg"));

            /* Choose File */
            File selectedFile = fileChooser.showOpenDialog(stage);
            FileInputStream inputstream = new FileInputStream(selectedFile);

            /* File Type Extraction */
            String fileName = selectedFile.getName();
            String fileExtension =
                    fileName.substring(fileName.lastIndexOf(".") + 1, selectedFile.getName().length());

            /* Message Factory */
            Message temp = new Message();
            Message tempInfo = new Message();
            temp.setImage(new Image(inputstream));
            temp.setExtension(fileExtension);
            temp.text = "img";
            tempInfo.setText(nickName + " sent an image (" + fileName +")\n");
            tempInfo.setSenderName("[Server]");
            /* Send Message */
            os.writeObject(temp);
            os.writeObject(tempInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void sendMessage(ActionEvent event) throws Exception {
        String msg = chatEntry.getText();

        if(msg != null) {
            Message toSend = new Message();
            toSend.setText(msg + "\n");
            toSend.setSenderID(id);
            toSend.setSenderName(nickName);
            os.writeObject(toSend);
            chatEntry.clear();
        }

    }

    @FXML
    void sendFile(ActionEvent event) {
        serializeFile();
    }

    @FXML
    void downloadFile(ActionEvent event) {
        String fileName = fileField.getText();
        if(fileName != null) {
            Message reqFile = new Message();
            reqFile.setText("reqFile");
            reqFile.setSenderName(nickName);
            reqFile.setFileName(fileName);

            try {
                os.writeObject(reqFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void receiveFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();

        String extension = "*." + msg.getExtension();
        System.out.println(extension);
        fileChooser.getExtensionFilters().add
                (new FileChooser.ExtensionFilter(msg.getExtension().toUpperCase(), extension));
        fileChooser.setInitialFileName(msg.getFileName());
        File selectedDirectory = fileChooser.showSaveDialog(stage);

        if(selectedDirectory != null)
            parseFile(selectedDirectory.getAbsolutePath());

    }

    void parseFile(String path) {
        try (FileOutputStream fos = new FileOutputStream(path)) {
            fos.write(msg.getFileBytes());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void logOut(ActionEvent event) {
        chatBox.clear();
        fileArea.clear();

        getBtn.setVisible(false);
        downloadBtn.setVisible(false);
        fileField.setVisible(false);
        logOutBtn.setVisible(false);
        statusLabel.setVisible(false);
        userLabel.setVisible(false);
        idLabel.setVisible(false);
        chatEntry.setVisible(false);
        chatBox.setVisible(false);
        sendBtn.setVisible(false);
        testButton.setVisible(false);
        appLabel.setVisible(false);
        imageBox.setVisible(false);
        fileArea.setVisible(false);
        sendFileBtn.setVisible(false);
        fileLabel.setVisible(false);
        pictureLabel.setVisible(false);

        addressField.setVisible(true);
        portField.setVisible(true);
        usernameField.setVisible(true);
        useridField.setVisible(true);
        connectButton.setVisible(true);
        loginLabel.setVisible(true);
        connectButton.setVisible(true);
        reconBtn.setVisible(false);

        this.serverAddress = null;
        this.port = 0;
        this.clientEndpoint = null;
        this.nickName = null;
        this.id = 0;

        controllerThread.reconBtn = false;
        try {
            this.os.close();
            this.is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /* Chat Functions */
    void showImage() {
        imageBox.setImage(image);
    }

    public void receiveMessage() {
        chatBox.appendText(msg.getSenderName() + ": " + msg.text);
    }

    public void serializeFile() {
        try {
            /* File Chooser and Filters*/
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose File to Send");

            /* Choose File */
            File selectedFile = fileChooser.showOpenDialog(stage);

            /* File Type Extraction */
            String fileName = selectedFile.getName();
            String fileExtension =
                    fileName.substring(fileName.lastIndexOf(".") + 1, selectedFile.getName().length());

            try {
                byte[] encoded = Files.readAllBytes(selectedFile.toPath());
                System.out.println(Arrays.toString(encoded));

                /* Message Factory */
                Message temp = new Message();
                temp.setExtension(fileExtension);
                temp.text = "file";
                temp.setFileName(fileName);
                temp.setSenderName(nickName);
                temp.setFileBytes(encoded);
                /* Send Message */
                os.writeObject(temp);

            } catch (IOException e) {
                e.printStackTrace();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* Initializations */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void initConn() {
        try {
            clientEndpoint = new Socket(serverAddress, port);
            System.out.println("Client: Connected to server at " + clientEndpoint.getRemoteSocketAddress());

            os = new ObjectOutputStream(clientEndpoint.getOutputStream());
            is = new ObjectInputStream(clientEndpoint.getInputStream());

            controllerThread = new ControllerThread(this);
            controllerThread.start();

            reconBtn.setVisible(false);
            addressField.setVisible(false);
            portField.setVisible(false);
            usernameField.setVisible(false);
            useridField.setVisible(false);
            connectButton.setVisible(false);
            loginLabel.setVisible(false);

            getBtn.setVisible(true);
            downloadBtn.setVisible(true);
            fileField.setVisible(true);
            fileArea.setVisible(true);
            sendFileBtn.setVisible(true);
            fileLabel.setVisible(true);
            pictureLabel.setVisible(true);
            userLabel.setVisible(true);
            idLabel.setVisible(true);
            logOutBtn.setVisible(true);
            statusLabel.setVisible(true);
            chatEntry.setVisible(true);
            chatBox.setVisible(true);
            sendBtn.setVisible(true);
            testButton.setVisible(true);
            appLabel.setVisible(true);
            imageBox.setVisible(true);

            chatBox.setEditable(false);
            fileArea.setEditable(false);

            statusLabel.setText("Status: Connected!");
            userLabel.setText("Username: " + nickName);
            idLabel.setText("ID number: " + id);
            chatBox.setWrapText(true);
            fileArea.setWrapText(true);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void initRecon(boolean bool) {

        reconBtn.setVisible(bool);

        fileArea.clear();

        getBtn.setVisible(false);
        downloadBtn.setVisible(false);
        fileField.setVisible(false);
        fileArea.setVisible(false);
        sendFileBtn.setVisible(false);
        fileLabel.setVisible(false);
        pictureLabel.setVisible(false);
        logOutBtn.setVisible(false);
        statusLabel.setVisible(false);
        userLabel.setVisible(false);
        idLabel.setVisible(false);
        chatEntry.setVisible(false);
        chatBox.setVisible(false);
        sendBtn.setVisible(false);
        testButton.setVisible(false);
        appLabel.setVisible(false);
    }

    public void updateFileArea(String fileName) {
        fileArea.appendText(fileName + "\n");
    }
}
