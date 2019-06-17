import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;

public class Main extends Application {

  private final int DisplayWidth = 400;
  private final int DisplayHeight = 250;
  private final int Width = 800;
  private final int Height = 500;

  private Processor processor = new Processor();
  private IOBridge ioBridge = new IOBridge();
  private Memory memory = new Memory();
  private Keyboard keyboard = new Keyboard();
  private TextOutput textOutput = new TextOutput();
  private Display display = new Display(DisplayWidth, DisplayHeight);

  public Main() {
    processor.setIOBridge(ioBridge);
    memory.setIOBridge(ioBridge);
    keyboard.setIOBridge(ioBridge);
    textOutput.setIOBridge(ioBridge);
    display.setIOBridge(ioBridge);
  }

  @Override
  public void start(Stage primaryStage) {
    // initialize
    Group root = new Group();

    Processor.ProcessorPane processorPane = processor.getPane();
    Memory.MemoryPane memoryPane = memory.getPane();
    Keyboard.KeyboardPane keyboardPane = keyboard.getPane();
    TextOutput.TextOutputPane textOutputPane = textOutput.getPane();
    Display.DisplayCanvas displayCanvas = display.getCanvas();

    root.getChildren().addAll(
        processorPane,
        memoryPane,
        keyboardPane,
        textOutputPane,
        displayCanvas
    );

    displayCanvas.setWidth(DisplayWidth);
    displayCanvas.setHeight(DisplayHeight);
    displayCanvas.setLayoutX(0);
    displayCanvas.setLayoutY(0);

    int w = DisplayWidth / 2;
    keyboardPane.setPrefSize(w, Height - DisplayHeight);
    keyboardPane.setLayoutX(0);
    keyboardPane.setLayoutY(DisplayHeight);

    textOutputPane.setPrefSize(w, Height - DisplayHeight);
    textOutputPane.setLayoutX(w);
    textOutputPane.setLayoutY(DisplayHeight);

    processorPane.setPrefSize(Width - DisplayWidth, DisplayHeight);
    processorPane.setLayoutX(DisplayWidth);
    processorPane.setLayoutY(0);

    memoryPane.setPrefSize(Width - DisplayWidth, Height - DisplayHeight);
    memoryPane.setLayoutX(DisplayWidth);
    memoryPane.setLayoutY(DisplayHeight);

    processorPane.configureLoadButton(primaryStage);

    Scene scene = new Scene(root, Width, Height);
    // add css file
    File file = new File("info/stylesheet.css");
    scene.getStylesheets().add("file:///" + file.getAbsolutePath().replace('\\', '/'));

    primaryStage.setTitle("DyVM");
    primaryStage.setScene(scene);
    primaryStage.show();
  }

}
