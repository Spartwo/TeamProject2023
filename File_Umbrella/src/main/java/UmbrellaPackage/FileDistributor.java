/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package UmbrellaPackage;

import com.jcraft.jsch.*;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Vector;

/**
 * @authors Team 19
 * @partialsource https://heptadecane.medium.com/file-transfer-via-java-sockets-e8d4f30703a5
 */
public class FileDistributor implements Initializable {

    // JavaFX fields
    @FXML
    public FontAwesomeIconView plusIcon;
    @FXML
    public FontAwesomeIconView sendIcon;
    @FXML
    public Button sendFilesBtn;
    @FXML
    public Button addBtn;
    @FXML
    public Button downloadSceneBtn;
    @FXML
    public Button btnUpload;
    @FXML
    public FontAwesomeIconView uploadIcn;
    @FXML
    public TreeView fileListView;
    private final ObservableList<File> selectedFiles = FXCollections.observableArrayList();
    private String FTP_SERVER = "n6dpm7grhgzdg.westeurope.azurecontainer.io";
    private final int FTP_PORT = 22;
    private final String FTP_USERNAME = "sftp";
    private final String FTP_PASSWORD = "teamproject2023";
    private final String REMOTE_DIRECTORY = "/upload/";
    // get folder id from folderID.txt and convert to string
    private String folderIdSend;

    public void handleSettingsWindow(MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/settings-view.fxml"));
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendFilesToSFTP() {
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(FTP_USERNAME, FTP_SERVER, FTP_PORT);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(FTP_PASSWORD);
            session.connect();

            ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();

            // send files to seperate folder on SFTP server based on folder id
            channel.cd(REMOTE_DIRECTORY + folderIdSend);

            // send files to SFTP server
            for (File file : selectedFiles) {
                channel.put(new FileInputStream(file), file.getName());
            }

            // javafx alert to show that files have been sent
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Files Sent");
            alert.setHeaderText("Upload Successful!");
            alert.setContentText("Your files have been sent.");
            alert.showAndWait();
            channel.disconnect();
            session.disconnect();
        } catch (JSchException | SftpException e) {
            e.printStackTrace();
            // handle exception here
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void handleFileUpload(ActionEvent event) throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {

            // read folder id from folderID.txt
            String folderId = "";
            File details = new File("folderID.txt");
            BufferedReader br = new BufferedReader(new FileReader(details));
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" ");
                folderId = parts[0];
                folderIdSend = folderId;


            try {
                // Check if the folder exists on the SFTP server
                JSch jsch = new JSch();
                Session session = jsch.getSession(FTP_USERNAME, FTP_SERVER, FTP_PORT);
                session.setConfig("StrictHostKeyChecking", "no");
                session.setPassword(FTP_PASSWORD);
                session.connect();

                ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
                channel.connect();

                Vector<ChannelSftp.LsEntry> folders = channel.ls(REMOTE_DIRECTORY);
                boolean folderExists = false;
                // check folderID.txt for folder id
                for (ChannelSftp.LsEntry folder : folders) {
                    if (folder.getFilename().equals(folderId)) {
                        folderExists = true;
                        break;
                    }
                }



                // navigate to the folder on the SFTP server
                channel.cd(REMOTE_DIRECTORY + folderId);

                // allow user to select files to upload
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Select File to Send");

                // Show the file chooser dialog and get the selected file
                File file = fileChooser.showOpenDialog(((Node) event.getSource()).getScene().getWindow());

                if (file != null) {
                    // Create a TreeItem for the selected file
                    TreeItem<File> fileItem = new TreeItem<>(file);
                    // Add the TreeItem to the root node
                    String fileName = file.getName();
                    // get file size and display it in the tree view

                    fileItem.setValue(new File(fileName));
                    fileListView.getRoot().getChildren().add(fileItem);
                    selectedFiles.add(file);
                    // auto expand the root node
                    fileListView.getRoot().setExpanded(true);
                    btnUpload.setVisible(false);
                    btnUpload.setDisable(true);
                    uploadIcn.setVisible(false);
                    addBtn.setVisible(true);
                    addBtn.setDisable(false);
                    sendIcon.setVisible(true);
                    sendFilesBtn.setVisible(true);


                    if (!folderExists) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Folder Not Found");
                        alert.setHeaderText("The folder you entered could not be found.");
                        alert.setContentText("Please enter a valid folder ID.");
                        alert.showAndWait();
                        return;
                    }

                    // Check if the target directory matches the selected folder ID
                    String targetDirectory = channel.pwd();
                    if (!targetDirectory.endsWith(folderId)) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Invalid Folder");
                        alert.setHeaderText("You can only upload files to the folder you selected.");
                        alert.setContentText("Please select a different folder or enter a different folder ID.");
                        alert.showAndWait();
                        return;
                    }

                    channel.disconnect();
                    session.disconnect();
                }

            } catch (JSchException e) {
                throw new RuntimeException(e);
            } catch (SftpException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /*public static void sendEnvelope(Envelope e) {
        
    }*/

    public void sendFile(MouseEvent event) {
        sendFilesToSFTP();
    }

    public void handleDownloadScene(ActionEvent event) throws IOException {
        // open download scene
        Parent root = FXMLLoader.load(getClass().getResource("/download-view.fxml"));
        Scene scene = new Scene(root);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        TreeItem<String> root = new TreeItem<>("Uploaded Files", new ImageView(new Image(getClass().getResourceAsStream("/Folder_Icon.png"))));
        fileListView.setRoot(root);
    }
}
