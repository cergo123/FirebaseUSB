package sample;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.cloud.FirestoreClient;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class Main extends Application {

    private Label message;
    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("login_layout.fxml"));
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();

        message = (Label) scene.lookup("#lb_login");
        AutoDetect autoDetect  = new AutoDetect();
        autoDetect.waitForNotifying(message);

        //Listening
        startListening(autoDetect, primaryStage);
    }

    private void startListening(AutoDetect autoDetect, Stage primaryStage) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    String usb = autoDetect.getUsb();
                    if (usb != null){
                        if (login(usb, autoDetect, primaryStage)){

                            break;
                        }
                    }else{
                        Platform.runLater(() -> message.setText("Please plug the USB to Login"));
                    }
                }
            }
        }).start();
    }

    private boolean login(String usb, AutoDetect autoDetect, Stage primaryStage) {
        File admin = new File(usb + File.separator + "admin.json");
        ArrayList<User> users = new ArrayList<>();
        try {
            Platform.runLater(() -> message.setText("Logging ..."));
            FileInputStream serviceAccount  = new FileInputStream(admin);
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    // Your Database URL can be found in your firebase console -> your project
                    .setDatabaseUrl("https://tutorial-8b2f6.firebaseio.com/")
                    .build();
            FirebaseApp finestayApp = null;
            boolean hasBeenInitialized=false;
            List<FirebaseApp> firebaseApps = FirebaseApp.getApps();
            for(FirebaseApp app : firebaseApps){
                if(app.getName().equals(FirebaseApp.DEFAULT_APP_NAME)){
                    hasBeenInitialized=true;
                    finestayApp = app;
                }
            }

            if(!hasBeenInitialized) {
                finestayApp = FirebaseApp.initializeApp(options);
            }
            String token = FirebaseAuth.getInstance().createCustomToken("8Ne5d8F1zFf1d2jS4qUHA4hd0nw1"); // This is the user UID that we created

            // Platform.runLater(() -> message.setText("Qeydan dikşîne ..."));
            Firestore db = FirestoreClient.getFirestore(finestayApp);
            ApiFuture<QuerySnapshot> future =
                    db.collection("users").get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            int i = 1;
            for (DocumentSnapshot document : documents) {
                int finalI = i;
                Platform.runLater(() -> message.setText("Fetching  data from DB: " + finalI + "/" + documents.size()));
                User user  = document.toObject(User.class);
                users.add(user);
                i ++;
            }
            FirebaseApp finalFinestayApp = finestayApp;
            Platform.runLater(() -> {
                message.setText("Done.");
                printUsersFromDB(users, usb, finalFinestayApp);

            });
            return true;
        } catch (IOException | FirebaseAuthException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            Platform.runLater(() -> message.setText("There was an error!"));
            return false;
        }
    }

    private void printUsersFromDB(ArrayList<User> users, String usb, FirebaseApp finalFinestayApp) {
        for (User user : users){
            System.out.println("Name: " + user.getName() + "\tEmail: " + user.getEmail());
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
