import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Processor {

  // normal speed
  private final static Duration duration1 = Duration.millis(1);
  // faster speed
  private final static Duration duration2 = Duration.millis(1);

  // running mode
  private int mode = 0;
  // 0: paused/halted/single-step running
  // 1: running at normal speed
  // 2: running at a faster speed

  /* virtual memory (abstract files) */
  private IOBridge ioBridge = null;

  private ProcessorPane pane = new ProcessorPane();

  // timeline for mode == 1
  private Timeline tlslow = new Timeline();
  // timeline for mode == 2
  private Timeline tlfast = new Timeline();
  // timeline for refreshing
  private Timeline tlref = new Timeline();

  public Processor() {
    // read instruction types
    Scanner in = null;
    try {
      in = new Scanner(new File("./info/ins_type.txt"));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    while (in.hasNext()) {
      int icode, type;
      icode = in.nextInt();
      type = in.nextInt();
      insTypes[icode] = type;
    }
    in.close();

    // init tlslow
    tlslow.setCycleCount(Timeline.INDEFINITE);
    tlslow.getKeyFrames().add(new KeyFrame(duration1,
        (e) -> {
          try {
            next();
          } catch (Exception e1) {
            handleException(e1);
          }
        })
    );
    // init tlfast
    tlfast.setCycleCount(Timeline.INDEFINITE);
    tlfast.getKeyFrames().add(new KeyFrame(duration2,
        (e) -> {
          try {
            for (int i = 0; i < 15000; i++) next();
          } catch (Exception e1) {
            handleException(e1);
          }
        })
    );
    // init tlref
    tlref.setCycleCount(Timeline.INDEFINITE);
    tlref.getKeyFrames().add(
        new KeyFrame(Duration.millis(50), e -> {
          try {
            refresh();
          } catch (Exception e1) {
            handleException(e1);
          }
        })
    );
    tlref.play();
  }

  private void handleException(Exception e) {
    if (mode == 1) tlslow.pause();
    else if (mode == 2) tlfast.pause();
    mode = 0;
    loaded = false;
    tlfast.stop();
    e.printStackTrace();
  }

  public void setIOBridge(IOBridge iobrg) {
    ioBridge = iobrg;
    ioBridge.setProcessor(this);
  }

  public ProcessorPane getPane() {
    return pane;
  }

  /* program counter */
  private int pc;

  /*
    conditional code
    cc[2]: ZF (zero)
    cc[1]: SF (signed)
    cc[0]: OF (overflow)
  */
  private byte cc;

  private boolean cond(int sel) {
    /* return the conditional state */
    boolean ret = true;
    boolean zf = false, sf = false, of = false;
    if ((cc & 4) > 0) zf = true;
    if ((cc & 2) > 0) sf = true;
    if ((cc & 1) > 0) of = true;
    switch (sel) {
      case 1: ret = zf; break; // e
      case 2: ret = !zf; break; // ne
      case 3: ret = (sf == of) && !zf; break; // g
      case 4: ret = (sf == of); break; // ge
      case 5: ret = sf ^ of; break; // l
      case 6: ret = (sf ^ of) || zf; break; // le
    }
    return ret;
  }

  /* state */
  private byte state;

  /* registers */
  private long[] regs = new long[15];
  final private int rsp = 4;

  /* instructions */
  private int[] insTypes = new int[15];

  private int insLen;
  private byte[] ir = new byte[10];

  /*
   * ******** DISPLAY ********************************
   */

  private void refresh() throws Exception {
    pane.refresh();
  }

  /*
   * ******** PROCESS ********************************
   */

  private boolean loaded = false;
  private int nextPC;

  private void fetch() throws Exception {
    /* fetch instruction */
    byte op = ioBridge.readb(pc + Memory.OBJECT_SECTION_POS);
    ir[0] = op;
    int type = insTypes[op >> 4 & 0xf];
    // 0: null
    // 1: r
    // 2: rr
    // 3: ri
    // 4: rri
    // 5: i

    switch (type) {
      case 0: insLen = 1; break;
      case 1: insLen = 2; break;
      case 2: insLen = 2; break;
      case 3: insLen = 10; break;
      case 4: insLen = 10; break;
      case 5: insLen = 9; break;
    }

    nextPC = pc + insLen;

    /* store it in instruction registers */
    for (int i = 1; i < insLen; i++) {
      ir[i] = ioBridge.readb(pc + i + Memory.OBJECT_SECTION_POS);
    }
  }

  private void exec() throws Exception {
    /* decode and exec */
    byte icode = (byte) (ir[0] >> 4 & 0xf);
    byte ifun = (byte) (ir[0] & 0xf);

    switch (icode) {
      case 0: runIns0(ifun); break;
      case 1: runIns1(ifun); break;
      case 2: runIns2(ifun); break;
      case 3: runIns3(ifun); break;
      case 4: runIns4(ifun); break;
      case 5: runIns5(ifun); break;
      case 6: runIns6(ifun); break;
      case 7: runIns7(ifun); break;
      case 8: runIns8(ifun); break;
      case 9: runIns9(ifun); break;
    }
    /* update pc */
    pc = nextPC;
  }

  private void init() throws Exception {
    /* initialize the processor for the newly loaded program */
    loaded = true;
    /* initialize program counter */
    pc = (int) ioBridge.readq(Memory.OBJECT_SECTION_POS);
    /* clear output buffer */
    ioBridge.setOutputBufferSize(0);
    /* clear the registers */
    for (int i = 0; i < 15; i++) {
      regs[i] = 0;
    }
    /* reset the stack register */
    regs[rsp] = Memory.STACK_SECTION_LIMIT;
    /* put a random seed in memory */
    ioBridge.setRandomSeed((int) System.currentTimeMillis() % 48271);
    ioBridge.clearDisplay();
    ioBridge.clearTextOutput();
  }

  private void next() throws Exception {
    /* run the next instruction cycle */
    if (loaded) {
      fetch();
      exec();
    }
  }

  private void runIns0(byte ifun) throws Exception {
    switch (ifun) {
      case 0:
        throw new Exception("HALT");
      case 1:
        break;
      case 2:
        nextPC = (int) ioBridge.readq((int) regs[rsp]);
        regs[rsp] += 8;
        break;
      case 3:
        // TODO
        break;
    }
  }

  private void runIns1(byte ifun) {
    if (ifun == 0) {
      byte rA = (byte) (ir[1] >> 4 & 0xf);
      long imme = 0;
      for (int i = 0; i < 8; i++) {
        imme += ((long)ir[i + 2] & 0xff) << (i << 3);
      }
      regs[rA] = imme;
    }
  }

  private void runIns2(byte ifun) throws Exception {
    byte rA = (byte) (ir[1] >> 4 & 0xf);
    byte rB = (byte) (ir[1] & 0xf);
    long imme = 0;
    for (int i = 0; i < 8; i++) {
      imme += ((long)ir[i + 2] & 0xff) << (i << 3);
    }
    int addr = (int) imme;
    if (rB != 0xf) addr += (int) regs[rB];
    switch (ifun) {
      case 0: ioBridge.writeq(addr, regs[rA]); break;
      case 1: ioBridge.writel(addr, (int) regs[rA]); break;
      case 2: ioBridge.writew(addr, (short) regs[rA]); break;
      case 3: ioBridge.writeb(addr, (byte) regs[rA]); break;
      case 4: regs[rA] = ioBridge.readq(addr); break;
      case 5: regs[rA] = 0xffffffffL & (long) ioBridge.readl(addr); break;
      case 6: regs[rA] = 0xffffL & (long) ioBridge.readw(addr); break;
      case 7: regs[rA] = 0xffL & (long) ioBridge.readb(addr); break;
    }
  }

  private void runIns3(byte ifun) {
    byte rA = (byte) (ir[1] >> 4 & 0xf);
    byte rB = (byte) (ir[1] & 0xf);
    boolean cnd = cond(ifun);
    if (cnd) regs[rB] = regs[rA];
  }

  private void runIns4(byte ifun) {
    byte rA = (byte) (ir[1] >> 4 & 0xf);
    byte rB = (byte) (ir[1] & 0xf);
    long vA = regs[rA];
    long vB = regs[rB];
    long vC = 0;

    boolean zf, sf, of = false;

    switch (ifun) {
      case 0: // add
        vC = vA + vB;
        of = (vA < 0 == vB < 0) && (vA < 0 != vC < 0);
        break;
      case 1: // sub
        vC = vA - vB;
        of = (vA < 0 != vB < 0) && (vA < 0 != vC < 0);
        break;
      case 2: // and
        vC = vA & vB; break;
      case 3: // or
        vC = vA | vB; break;
      case 4: // xor
        vC = vA ^ vB; break;
      case 5: // sal
        vC = vA << (vB & 0x3f); break;
      case 6: // sar
        vC = vA >> (vB & 0x3f); break;
      case 7: // shr
        vC = vA >>> (vB & 0x3f); break;
      case 8: // mul
        vC = vA * vB; break;
      case 9: // idiv
        vC = vA / vB;
        regs[0] = vA % vB;
        if (rA == 0) vC = regs[0];
        break;
    }

    zf = vC == 0;
    sf = vC < 0;
    regs[rA] = vC;

    cc = 0;
    if (zf) cc |= 4;
    if (sf) cc |= 2;
    if (of) cc |= 1;
  }

  private void runIns5(byte ifun) {
    byte rA = (byte) (ir[1] >> 4 & 0xf);
    long vA = regs[rA];
    long vC = 0;

    boolean zf, sf, of = false;
    switch (ifun) {
      case 0: vC = ~vA; break; // not
      case 1: vC = -vA; break; // neg
      case 2:
        vC = vA + 1;
        of = vC < 0 && vA > 0;
        break; // inc
      case 3:
        vC = vA - 1;
        of = vC > 0 && vA < 0;
        break; // dec
      case 4: vC = (long) (int) vA; break; // cltq
      case 5: vC = (long) (short) vA; break; // cwtq
      case 6: vC = (long) (byte) vA; break; // cbtq
      case 7: vC = vA & 0xffffffffL; break; // cqtl
      case 8: vC = vA & 0xffffL; break; // cqtw
      case 9: vC = vA & 0xffL; break; // cqtb
    }

    regs[rA] = vC;

    zf = (vC == 0);
    sf = (vC < 0);
    cc = 0;
    if (zf) cc |= 4;
    if (sf) cc |= 2;
    if (of) cc |= 1;
  }

  private void runIns6(byte ifun) {
    byte rA = (byte) (ir[1] >> 4 & 0xf);
    byte rB = (byte) (ir[1] & 0xf);
    long vA = regs[rA];
    long vB = regs[rB];
    long vC = 0;

    boolean sf, zf, of = false;

    switch (ifun) {
      case 0: // cmp
        vC = vA - vB;
        of = (vA < 0 != vB < 0) && (vA < 0 != vC < 0);
        break;
      case 1: // test
        vC = vA & vB;
        break;
    }

    zf = (vC == 0);
    sf = (vC < 0);
    cc = 0;
    if (zf) cc |= 4;
    if (sf) cc |= 2;
    if (of) cc |= 1;
  }

  private void runIns7(byte ifun) throws Exception {
    /* branch ins */
    int target = 0;
    for (int i = 0; i < 4; i++) {
      target += (((int)ir[i + 1] & 0xff) << (i << 3));
    }
    boolean cnd = cond(ifun);
    if (ifun == 7) {
      ioBridge.writeq((int) (regs[rsp] - 8), nextPC);
      regs[rsp] -= 8;
    }
    if (cnd) nextPC = target;
  }

  private void runIns8(byte ifun) throws Exception {
    byte rA = (byte) (ir[1] >> 4 & 0xf);
    if (ifun == 0) {    // push
      ioBridge.writeq((int) (regs[rsp] - 8), regs[rA]);
      regs[rsp] -= 8;
    } else if (ifun == 1) {  // pop
      long vA = ioBridge.readq((int) regs[rsp]);
      regs[rsp] += 8;
      regs[rA] = vA;
    }
  }

  private void runIns9(byte ifun) throws Exception {
    /* interrupt */
    long imme = 0;
    for (int i = 0; i < 8; i++) {
      imme += ((long)ir[1 + i] & 0xff) << (i << 3);
    }
    ioBridge.interupt((int) imme);
  }

  /*
   * *********** END PROCESS ***********************
   */


  public class ProcessorPane extends BorderPane {
    /* ControlPane of the Processor */

    private Button btLoad = new Button("Load");
    private Button btRun = new Button("Run");
    private Button btFast = new Button("Fast"); // execute at a faster speed
    private Button btPause = new Button("Pause");
    private Button btStep = new Button("Step"); // single step execution
    private Button btHalt = new Button("Halt");

    private Label[] registers = new Label[15];
    private Label PCLabel;

    private GridPane centerPane = new GridPane();
    private HBox bottomPane = new HBox();

    public ProcessorPane() {
      configureBottomPane();
      bottomPane.getChildren().addAll(btLoad, btRun, btFast, btPause, btStep, btHalt);
      bottomPane.setId("bottom-pane");
      setBottom(bottomPane);

      configureCenterPane();
      centerPane.setId("center-pane");
      setCenter(centerPane);
    }

    private void configureCenterPane() {
      /* show registers */
      String[] regNames = {
          "%rax", "%rcx", "%rdx", "%rbx", "%rsp", "%rbp", "%rsi", "%rdi",
          "%r8", "%r9", "%r10", "%r11", "%r12", "%r13", "%r14",
      };
      final int nameWidth = 50;
      for (int i = 0; i < 15; i++) {
        /* show register name */
        Label name = new Label(regNames[i]);
        name.setId("register-name");
        /* show register value */
        Label label = new Label();
        label.setId("register-value");
        centerPane.widthProperty().addListener(ov -> {
          name.setPrefWidth(nameWidth);
          label.setPrefWidth(centerPane.getWidth() / 2 - nameWidth);
        });
        centerPane.heightProperty().addListener(ov -> {
          name.setPrefHeight(centerPane.getHeight() / 8);
          label.setPrefHeight(centerPane.getHeight() / 8);
        });
        registers[i] = label;
        centerPane.add(name, i / 8 * 2, i % 8);
        centerPane.add(label, i / 8 * 2 + 1, i % 8);
      }
      /* show pc */
      Label PCName = new Label("PC");
      PCName.setId("pc-name");
      PCLabel = new Label();
      PCLabel.setId("pc-value");
      centerPane.widthProperty().addListener(ov -> {
        PCName.setPrefWidth(nameWidth);
        PCLabel.setPrefWidth(centerPane.getWidth() / 2 - nameWidth);
      });
      centerPane.heightProperty().addListener(ov -> {
        PCName.setPrefHeight(centerPane.getHeight() / 8);
        PCLabel.setPrefHeight(centerPane.getHeight() / 8);
      });

      centerPane.add(PCName, 2, 7);
      centerPane.add(PCLabel, 3, 7);
    }

    private void configureBottomPane() {
      bottomPane.widthProperty().addListener(ov -> {
        btLoad.setPrefWidth(getWidth() / 6);
        btRun.setPrefWidth(getWidth() / 6);
        btFast.setPrefWidth(getWidth() / 6);
        btPause.setPrefWidth(getWidth() / 6);
        btStep.setPrefWidth(getWidth() / 6);
        btHalt.setPrefWidth(getWidth() / 6);
      });

      btRun.setOnAction(e -> {
        if (mode == 2) tlfast.pause();
        mode = 1;
        tlslow.play();
      });
      btFast.setOnAction(e -> {
        if (mode == 1) tlslow.pause();
        mode = 2;
        tlfast.play();
      });

      btPause.setOnAction(e -> {
        if (mode == 1) {
          tlslow.pause();
        } else if (mode == 2) {
          tlfast.pause();
        }
        mode = 0;
      });

      btHalt.setOnAction(e -> {
        if (mode == 1) {
          tlslow.stop();
        } else if (mode == 2) {
          tlfast.stop();
        }
        mode = 0;
        try {
          if (loaded) init();
        } catch (Exception e1) {
          e1.printStackTrace();
        }
      });

      btStep.setOnAction(e -> {
        if (mode == 1) tlslow.pause();
        else if (mode == 2) tlfast.pause();
        mode = 0;
        try {
          next();
        } catch (Exception e1) {
          handleException(e1);
        }
      });
    }

    public void configureLoadButton(Stage stage) {
      FileChooser fileChooser = new FileChooser();
      fileChooser.setTitle("Choose object file");
      fileChooser.setInitialDirectory(new File("./hex/"));
      fileChooser.getExtensionFilters().add(
          new FileChooser.ExtensionFilter("HEX", "*.hex")
      );
      /* load object file */
      btLoad.setOnAction((e) -> {
        if (mode == 1) tlslow.stop();
        else if (mode == 2) tlfast.stop();
        mode = 0;
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
          try {
            ioBridge.loadObject(file);
            init();
          } catch (Exception e1) {
            e1.printStackTrace();
          }
        }
      });
    }

    public void refresh() throws Exception {
      /* update the values of registers and memory units */
      for (int i = 0; i < 15; i++) {
        registers[i].setText(Long.toString(regs[i]));
      }
      PCLabel.setText(Integer.toHexString(pc));
      ioBridge.refresh();
    }
  }
}
