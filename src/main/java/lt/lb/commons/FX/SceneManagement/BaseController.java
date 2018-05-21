/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.FX.SceneManagement;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;

/**
 *
 * @author Laimonas Beniušis
 */
public interface BaseController extends Initializable {

    @Override
    public default void initialize(URL url, ResourceBundle rb) {
    }

    public default void exit() {
    }

    public default void update() {
    }

    public void initialize();
}
