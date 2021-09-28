package com.dlsusap.chat.app.model;

import java.io.*;

import javax.imageio.ImageIO;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

public class Message implements Serializable {
    private static final long serialVersionUID = 8L;

    private transient Image image;
    private byte[] fileBytes;
    private String fileName;
    public String text;
    String extension;

    private String senderName;
    private int senderID;
    private int receiverID;

    public Message() {

    }

    public String getText() {
        return text;
    }

    public int getSenderID() {
        return senderID;
    }

    public String getSenderName() {
        return senderName;
    }

    public Image getImage() {
        return image;
    }

    public int getReceiverID() {
        return receiverID;
    }

    public String getExtension() {
        return extension;
    }

    public byte[] getFileBytes() {
        return fileBytes;
    }

    public String getFileName() {
        return fileName;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setReceiverID(int receiverID) {
        this.receiverID = receiverID;
    }

    public void setSenderID(int senderID) {
        this.senderID = senderID;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFileBytes(byte[] fileBytes) {
        this.fileBytes = fileBytes;
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();

        if (s.readBoolean()) {
            image = new Image(s);
        } else {
            image = null;
        }
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();

        s.writeBoolean(image != null);
        if (image != null)
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), extension, s);
    }
}
