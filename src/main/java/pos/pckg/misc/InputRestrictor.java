package pos.pckg.misc;

import javafx.scene.control.TextField;

public class InputRestrictor {

    public static void numbersInput(TextField textField){
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.matches("\\d*\\.")) return;
            textField.setText(newValue.replaceAll("[^\\d\\.]", ""));
        });
    }

    public static void limitInput(TextField textField,int max){
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length()>max)
                textField.setText(newValue.substring(0,max));
            else
                return;
        });
    }

    public static void withouDecimal(TextField textField){
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.matches("\\d*")) return;
            textField.setText(newValue.replaceAll("[^\\d]", ""));
        });
    }

}
