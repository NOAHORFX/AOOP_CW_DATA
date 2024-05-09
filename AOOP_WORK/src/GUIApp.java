import Controller.NumberleController;
import Model.INumberleModel;
import Model.NumberleModel;
import View.NumberleView;

import javax.swing.*;

public class GUIApp {
    public static void main(String[] args) {
//        try {
//            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        javax.swing.SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        createAndShowGUI();
                    }
                }
        );
    }

    public static void createAndShowGUI() {
        INumberleModel model = new NumberleModel();
        NumberleController controller = new NumberleController(model);
        NumberleView view = new NumberleView(model, controller);
    }
}