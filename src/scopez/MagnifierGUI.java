package scopez;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseListener;

public class MagnifierGUI extends JFrame implements NativeKeyListener, NativeMouseListener {

    private final MagnifierWrapper magnifier;
    private JTextField tfWidth;
    private JTextField tfHeight;
    private JCheckBox cbCircular;
    private JTextField tfRefreshRate;
    private JTextField tfXOffset;
    private JTextField tfYOffset;
    private JSlider sliderWidth;
    private JSlider sliderHeight;
    private JSlider sliderRefreshRate;
    private JSlider sliderXOffset;
    private JSlider sliderYOffset;
    private JTextField tfHotKey;
    private JRadioButton rbToggle;
    private JRadioButton rbHold;
    private int hotKeyCode = NativeKeyEvent.VC_X;
    private boolean hotKeyIsMouse = false;
    private int hotKeyMouseButton = 0;
    private JTextField tfZoomMultiplierHotKey;
    private JTextField tfZoomLow;
    private JTextField tfZoomHigh;
    private int zoomHotKeyCode = NativeKeyEvent.VC_Z;
    private boolean zoomHotKeyIsMouse = false;
    private int zoomHotKeyMouseButton = 0;
    private boolean zoomMultiplierState = false;
    private boolean windowVisible = false;
    private JCheckBox cbDisplayOffset;
    private OffsetOverlay offsetOverlay;
    private boolean zoomKeyPressed = false;

    public MagnifierGUI() {
        magnifier = new MagnifierWrapper();
        if (!magnifier.nativeInit()) {
            System.err.println("Magnifier initialization error.");
            System.exit(1);
        }
        setUndecorated(true);
        setLocation(0, 0);
        initComponents();
        setAlwaysOnTop(true);
        initGlobalKeyListener();
        loadSettings();
        applySettings();
    }

    private void initComponents() {
        setTitle("Magnifier Settings");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(43, 43, 43));
        add(mainPanel);
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(43, 43, 43));
        JLabel titleLabel = new JLabel("ScopeZ", JLabel.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        JLabel tipLabel = new JLabel("Toggle this menu (INSERT)", JLabel.CENTER);
        tipLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        tipLabel.setForeground(Color.LIGHT_GRAY);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(tipLabel, BorderLayout.SOUTH);
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        JPanel settingsPanel = new JPanel();
        settingsPanel.setBackground(new Color(43, 43, 43));
        TitledBorder settingsBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Window Settings");
        settingsBorder.setTitleColor(Color.WHITE);
        settingsPanel.setBorder(settingsBorder);
        settingsPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        Font font = new Font("SansSerif", Font.PLAIN, 14);
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel lblWidth = new JLabel("Width:");
        lblWidth.setForeground(Color.WHITE);
        lblWidth.setFont(font);
        settingsPanel.add(lblWidth, gbc);
        gbc.gridx = 1;
        tfWidth = new JTextField("400", 5);
        tfWidth.setFont(font);
        settingsPanel.add(tfWidth, gbc);
        gbc.gridx = 2;
        sliderWidth = new JSlider(100, 2000, 400);
        sliderWidth.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                tfWidth.setText(String.valueOf(sliderWidth.getValue()));
            }
        });
        settingsPanel.add(sliderWidth, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel lblHeight = new JLabel("Height:");
        lblHeight.setForeground(Color.WHITE);
        lblHeight.setFont(font);
        settingsPanel.add(lblHeight, gbc);
        gbc.gridx = 1;
        tfHeight = new JTextField("400", 5);
        tfHeight.setFont(font);
        settingsPanel.add(tfHeight, gbc);
        gbc.gridx = 2;
        sliderHeight = new JSlider(100, 2000, 400);
        sliderHeight.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                tfHeight.setText(String.valueOf(sliderHeight.getValue()));
            }
        });
        settingsPanel.add(sliderHeight, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel lblCircular = new JLabel("Circular Shape:");
        lblCircular.setForeground(Color.WHITE);
        lblCircular.setFont(font);
        settingsPanel.add(lblCircular, gbc);
        gbc.gridx = 1;
        cbCircular = new JCheckBox();
        cbCircular.setSelected(true);
        cbCircular.setBackground(new Color(43, 43, 43));
        cbCircular.setForeground(Color.WHITE);
        settingsPanel.add(cbCircular, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel lblDisplayOffset = new JLabel("Display Offset:");
        lblDisplayOffset.setForeground(Color.WHITE);
        lblDisplayOffset.setFont(font);
        settingsPanel.add(lblDisplayOffset, gbc);
        gbc.gridx = 1;
        cbDisplayOffset = new JCheckBox();
        cbDisplayOffset.setSelected(false);
        cbDisplayOffset.setBackground(new Color(43, 43, 43));
        cbDisplayOffset.setForeground(Color.WHITE);
        settingsPanel.add(cbDisplayOffset, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel lblFPS = new JLabel("Refresh Rate (FPS):");
        lblFPS.setForeground(Color.WHITE);
        lblFPS.setFont(font);
        settingsPanel.add(lblFPS, gbc);
        gbc.gridx = 1;
        tfRefreshRate = new JTextField("60", 5);
        tfRefreshRate.setFont(font);
        settingsPanel.add(tfRefreshRate, gbc);
        gbc.gridx = 2;
        sliderRefreshRate = new JSlider(1, 144, 60);
        sliderRefreshRate.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                tfRefreshRate.setText(String.valueOf(sliderRefreshRate.getValue()));
            }
        });
        settingsPanel.add(sliderRefreshRate, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel lblXOffset = new JLabel("Offset X:");
        lblXOffset.setForeground(Color.WHITE);
        lblXOffset.setFont(font);
        settingsPanel.add(lblXOffset, gbc);
        gbc.gridx = 1;
        tfXOffset = new JTextField("0", 5);
        tfXOffset.setFont(font);
        settingsPanel.add(tfXOffset, gbc);
        gbc.gridx = 2;
        sliderXOffset = new JSlider(-100, 100, 0);
        sliderXOffset.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                tfXOffset.setText(String.valueOf(sliderXOffset.getValue()));
            }
        });
        settingsPanel.add(sliderXOffset, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel lblYOffset = new JLabel("Offset Y:");
        lblYOffset.setForeground(Color.WHITE);
        lblYOffset.setFont(font);
        settingsPanel.add(lblYOffset, gbc);
        gbc.gridx = 1;
        tfYOffset = new JTextField("0", 5);
        tfYOffset.setFont(font);
        settingsPanel.add(tfYOffset, gbc);
        gbc.gridx = 2;
        sliderYOffset = new JSlider(-100, 100, 0);
        sliderYOffset.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                tfYOffset.setText(String.valueOf(sliderYOffset.getValue()));
            }
        });
        settingsPanel.add(sliderYOffset, gbc);
        JPanel hotKeyPanel = new JPanel();
        hotKeyPanel.setBackground(new Color(43, 43, 43));
        TitledBorder hotKeyBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Hotkey (Toggle/Hold)");
        hotKeyBorder.setTitleColor(Color.WHITE);
        hotKeyPanel.setBorder(hotKeyBorder);
        hotKeyPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbcHot = new GridBagConstraints();
        gbcHot.insets = new Insets(5, 5, 5, 5);
        gbcHot.fill = GridBagConstraints.HORIZONTAL;
        gbcHot.anchor = GridBagConstraints.WEST;
        gbcHot.gridx = 0;
        gbcHot.gridy = 0;
        JLabel lblHotKey = new JLabel("Press a key:");
        lblHotKey.setForeground(Color.WHITE);
        lblHotKey.setFont(font);
        hotKeyPanel.add(lblHotKey, gbcHot);
        gbcHot.gridx = 1;
        tfHotKey = new JTextField("X", 5);
        tfHotKey.setEditable(false);
        tfHotKey.setFont(font);
        hotKeyPanel.add(tfHotKey, gbcHot);
        gbcHot.gridx = 0;
        gbcHot.gridy = 1;
        rbToggle = new JRadioButton("Toggle", true);
        rbToggle.setBackground(new Color(43, 43, 43));
        rbToggle.setForeground(Color.WHITE);
        rbToggle.setFont(font);
        hotKeyPanel.add(rbToggle, gbcHot);
        gbcHot.gridx = 1;
        rbHold = new JRadioButton("Hold");
        rbHold.setBackground(new Color(43, 43, 43));
        rbHold.setForeground(Color.WHITE);
        rbHold.setFont(font);
        hotKeyPanel.add(rbHold, gbcHot);
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbToggle);
        bg.add(rbHold);
        JPanel zoomMultiplierPanel = new JPanel();
        zoomMultiplierPanel.setBackground(new Color(43, 43, 43));
        TitledBorder zoomBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Zoom Multiplier Settings");
        zoomBorder.setTitleColor(Color.WHITE);
        zoomMultiplierPanel.setBorder(zoomBorder);
        zoomMultiplierPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbcZoom = new GridBagConstraints();
        gbcZoom.insets = new Insets(5, 5, 5, 5);
        gbcZoom.fill = GridBagConstraints.HORIZONTAL;
        gbcZoom.anchor = GridBagConstraints.WEST;
        gbcZoom.gridx = 0;
        gbcZoom.gridy = 0;
        JLabel lblZoomHotKey = new JLabel("Hotkey:");
        lblZoomHotKey.setForeground(Color.WHITE);
        lblZoomHotKey.setFont(font);
        zoomMultiplierPanel.add(lblZoomHotKey, gbcZoom);
        gbcZoom.gridx = 1;
        tfZoomMultiplierHotKey = new JTextField("Z", 5);
        tfZoomMultiplierHotKey.setEditable(false);
        tfZoomMultiplierHotKey.setFont(font);
        zoomMultiplierPanel.add(tfZoomMultiplierHotKey, gbcZoom);
        gbcZoom.gridx = 0;
        gbcZoom.gridy++;
        JLabel lblZoomLow = new JLabel("Zoom value 1:");
        lblZoomLow.setForeground(Color.WHITE);
        lblZoomLow.setFont(font);
        zoomMultiplierPanel.add(lblZoomLow, gbcZoom);
        gbcZoom.gridx = 1;
        tfZoomLow = new JTextField("2.0", 5);
        tfZoomLow.setFont(font);
        zoomMultiplierPanel.add(tfZoomLow, gbcZoom);
        gbcZoom.gridx = 0;
        gbcZoom.gridy++;
        JLabel lblZoomHigh = new JLabel("Zoom value 2:");
        lblZoomHigh.setForeground(Color.WHITE);
        lblZoomHigh.setFont(font);
        zoomMultiplierPanel.add(lblZoomHigh, gbcZoom);
        gbcZoom.gridx = 1;
        tfZoomHigh = new JTextField("4.0", 5);
        tfZoomHigh.setFont(font);
        zoomMultiplierPanel.add(tfZoomHigh, gbcZoom);
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setBackground(new Color(43, 43, 43));
        JButton btnApply = new JButton("Apply Settings");
        btnApply.setFont(font);
        btnApply.addActionListener((ActionEvent e) -> {
            saveSettings();
            applySettings();
        });
        buttonsPanel.add(btnApply);
        JButton btnExit = new JButton("Exit");
        btnExit.setFont(font);
        btnExit.addActionListener((ActionEvent e) -> {
            System.exit(0);
        });
        buttonsPanel.add(btnExit);
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(43, 43, 43));
        topPanel.add(hotKeyPanel, BorderLayout.NORTH);
        topPanel.add(settingsPanel, BorderLayout.CENTER);
        topPanel.add(zoomMultiplierPanel, BorderLayout.SOUTH);
        mainPanel.add(topPanel, BorderLayout.CENTER);
        mainPanel.add(buttonsPanel, BorderLayout.SOUTH);
        tfHotKey.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                tfHotKey.setText("Press a key...");
            }
        });
        tfZoomMultiplierHotKey.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                tfZoomMultiplierHotKey.setText("Press a key...");
            }
        });
        pack();
    }

    private void applySettings() {
        try {
            int width = Integer.parseInt(tfWidth.getText());
            int height = Integer.parseInt(tfHeight.getText());
            int refresh = Integer.parseInt(tfRefreshRate.getText());
            int xOffset = Integer.parseInt(tfXOffset.getText());
            int yOffset = Integer.parseInt(tfYOffset.getText());
            magnifier.nativeSetResolution(width, height);
            magnifier.nativeSetWindowShape(cbCircular.isSelected());
            magnifier.nativeSetRefreshRate(refresh);
            magnifier.nativeMoveWindow(xOffset, yOffset);
            zoomToggled(zoomMultiplierState);
            if (cbDisplayOffset.isSelected()) {
                magnifier.nativeHideWindow();
                if (offsetOverlay == null) {
                    offsetOverlay = new OffsetOverlay(width, height, xOffset, yOffset, cbCircular.isSelected());
                } else {
                    offsetOverlay.updateSettings(width, height, xOffset, yOffset, cbCircular.isSelected());
                }
                offsetOverlay.setVisible(true);
            } else {
                if (offsetOverlay != null) {
                    offsetOverlay.setVisible(false);
                }
            }
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        }
    }

    private void initGlobalKeyListener() {
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.WARNING);
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        GlobalScreen.addNativeKeyListener(this);
        GlobalScreen.addNativeMouseListener(this);
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        int code = e.getKeyCode();
        if ("Press a key...".equals(tfHotKey.getText())) {
            hotKeyIsMouse = false;
            hotKeyCode = code;
            tfHotKey.setText(NativeKeyEvent.getKeyText(hotKeyCode));
            return;
        }
        if ("Press a key...".equals(tfZoomMultiplierHotKey.getText())) {
            zoomHotKeyIsMouse = false;
            zoomHotKeyCode = code;
            tfZoomMultiplierHotKey.setText(NativeKeyEvent.getKeyText(zoomHotKeyCode));
            return;
        }
        if (code == NativeKeyEvent.VC_INSERT) {
            setVisible(!this.isVisible());
            return;
        }
        if (!hotKeyIsMouse && code == hotKeyCode) {
            if (rbToggle.isSelected()) {
                if (windowVisible) {
                    if (cbDisplayOffset.isSelected() && offsetOverlay != null) {
                        offsetOverlay.setVisible(false);
                    } else {
                        magnifier.nativeHideWindow();
                    }
                } else {
                    if (cbDisplayOffset.isSelected() && offsetOverlay != null) {
                        offsetOverlay.setVisible(true);
                    } else {
                        magnifier.nativeShowWindow();
                    }
                }
                windowVisible = !windowVisible;
            } else if (rbHold.isSelected()) {
                if (cbDisplayOffset.isSelected() && offsetOverlay != null) {
                    offsetOverlay.setVisible(true);
                } else {
                    magnifier.nativeShowWindow();
                }
                windowVisible = true;
            }
        }
        if (!zoomHotKeyIsMouse && code == zoomHotKeyCode) {
            if (!zoomKeyPressed) {
                zoomMultiplierState = !zoomMultiplierState;
                zoomToggled(zoomMultiplierState);
                zoomKeyPressed = true;
            }
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        if (!hotKeyIsMouse && rbHold.isSelected() && e.getKeyCode() == hotKeyCode) {
            if (cbDisplayOffset.isSelected() && offsetOverlay != null) {
                offsetOverlay.setVisible(false);
            } else {
                magnifier.nativeHideWindow();
            }
            windowVisible = false;
        }
        if (!zoomHotKeyIsMouse && e.getKeyCode() == zoomHotKeyCode) {
            zoomKeyPressed = false;
        }
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {
    }

    @Override
    public void nativeMouseClicked(NativeMouseEvent e) {
    }

    @Override
    public void nativeMousePressed(NativeMouseEvent e) {
        if ("Press a key...".equals(tfHotKey.getText())) {
            hotKeyIsMouse = true;
            hotKeyMouseButton = e.getButton();
            String buttonText = e.getButton() == NativeMouseEvent.BUTTON1 ? "Left Button" : e.getButton() == NativeMouseEvent.BUTTON2 ? "Right Button" : e.getButton() == NativeMouseEvent.BUTTON3 ? "Middle Button" : "Mouse Button " + e.getButton();
            tfHotKey.setText(buttonText);
            return;
        }
        if ("Press a key...".equals(tfZoomMultiplierHotKey.getText())) {
            zoomHotKeyIsMouse = true;
            zoomHotKeyMouseButton = e.getButton();
            String buttonText = e.getButton() == NativeMouseEvent.BUTTON1 ? "Left Button" : e.getButton() == NativeMouseEvent.BUTTON2 ? "Right Button" : e.getButton() == NativeMouseEvent.BUTTON3 ? "Middle Button" : "Mouse Button " + e.getButton();
            tfZoomMultiplierHotKey.setText(buttonText);
            return;
        }
        if (hotKeyIsMouse && e.getButton() == hotKeyMouseButton) {
            if (rbToggle.isSelected()) {
                if (windowVisible) {
                    if (cbDisplayOffset.isSelected() && offsetOverlay != null) {
                        offsetOverlay.setVisible(false);
                    } else {
                        magnifier.nativeHideWindow();
                    }
                } else {
                    if (cbDisplayOffset.isSelected() && offsetOverlay != null) {
                        offsetOverlay.setVisible(true);
                    } else {
                        magnifier.nativeShowWindow();
                    }
                }
                windowVisible = !windowVisible;
            } else if (rbHold.isSelected()) {
                if (cbDisplayOffset.isSelected() && offsetOverlay != null) {
                    offsetOverlay.setVisible(true);
                } else {
                    magnifier.nativeShowWindow();
                }
                windowVisible = true;
            }
        }
        if (zoomHotKeyIsMouse && e.getButton() == zoomHotKeyMouseButton) {
            if (!zoomKeyPressed) {
                zoomMultiplierState = !zoomMultiplierState;
                zoomToggled(zoomMultiplierState);
                zoomKeyPressed = true;
            }
        }
    }

    @Override
    public void nativeMouseReleased(NativeMouseEvent e) {
        if (hotKeyIsMouse && rbHold.isSelected() && e.getButton() == hotKeyMouseButton) {
            if (cbDisplayOffset.isSelected() && offsetOverlay != null) {
                offsetOverlay.setVisible(false);
            } else {
                magnifier.nativeHideWindow();
            }
            windowVisible = false;
        }
        if (zoomHotKeyIsMouse && e.getButton() == zoomHotKeyMouseButton) {
            zoomKeyPressed = false;
        }
    }

    private void zoomToggled(boolean zoomMultiplierState) {
        try {
            double lowValue = Double.parseDouble(tfZoomLow.getText());
            double highValue = Double.parseDouble(tfZoomHigh.getText());
            double newZoom;
            if (!zoomMultiplierState) {
                newZoom = highValue;
            } else {
                newZoom = lowValue;
            }
            magnifier.nativeSetZoom(newZoom);
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        }
    }

    private void saveSettings() {
        Properties props = new Properties();
        props.setProperty("width", tfWidth.getText());
        props.setProperty("height", tfHeight.getText());
        props.setProperty("circular", Boolean.toString(cbCircular.isSelected()));
        props.setProperty("refreshRate", tfRefreshRate.getText());
        props.setProperty("xOffset", tfXOffset.getText());
        props.setProperty("yOffset", tfYOffset.getText());
        props.setProperty("hotKeyCode", Integer.toString(hotKeyCode));
        props.setProperty("hotKeyText", tfHotKey.getText());
        props.setProperty("mode", rbToggle.isSelected() ? "toggle" : "hold");
        props.setProperty("zoomHotKeyCode", Integer.toString(zoomHotKeyCode));
        props.setProperty("zoomHotKeyText", tfZoomMultiplierHotKey.getText());
        props.setProperty("zoomLow", tfZoomLow.getText());
        props.setProperty("zoomHigh", tfZoomHigh.getText());
        props.setProperty("displayOffset", Boolean.toString(cbDisplayOffset.isSelected()));
        try (FileOutputStream fos = new FileOutputStream("scopez_settings.properties")) {
            props.store(fos, null);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void loadSettings() {
        Properties props = new Properties();
        File file = new File("scopez_settings.properties");
        if (!file.exists()) {
            return;
        }
        try (FileInputStream fis = new FileInputStream(file)) {
            props.load(fis);
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
        tfWidth.setText(props.getProperty("width", "400"));
        tfHeight.setText(props.getProperty("height", "400"));
        cbCircular.setSelected(Boolean.parseBoolean(props.getProperty("circular", "true")));
        tfRefreshRate.setText(props.getProperty("refreshRate", "60"));
        tfXOffset.setText(props.getProperty("xOffset", "0"));
        tfYOffset.setText(props.getProperty("yOffset", "0"));
        try {
            hotKeyCode = Integer.parseInt(props.getProperty("hotKeyCode", Integer.toString(NativeKeyEvent.VC_X)));
        } catch (NumberFormatException ex) {
            hotKeyCode = NativeKeyEvent.VC_X;
        }
        tfHotKey.setText(props.getProperty("hotKeyText", NativeKeyEvent.getKeyText(NativeKeyEvent.VC_X)));
        String mode = props.getProperty("mode", "toggle");
        if ("toggle".equals(mode)) {
            rbToggle.setSelected(true);
        } else {
            rbHold.setSelected(true);
        }
        try {
            zoomHotKeyCode = Integer.parseInt(props.getProperty("zoomHotKeyCode", Integer.toString(NativeKeyEvent.VC_Z)));
        } catch (NumberFormatException ex) {
            zoomHotKeyCode = NativeKeyEvent.VC_Z;
        }
        tfZoomMultiplierHotKey.setText(props.getProperty("zoomHotKeyText", NativeKeyEvent.getKeyText(NativeKeyEvent.VC_Z)));
        tfZoomLow.setText(props.getProperty("zoomLow", "2.0"));
        tfZoomHigh.setText(props.getProperty("zoomHigh", "4.0"));
        cbDisplayOffset.setSelected(Boolean.parseBoolean(props.getProperty("displayOffset", "false")));
        try {
            int width = Integer.parseInt(tfWidth.getText());
            int height = Integer.parseInt(tfHeight.getText());
            sliderWidth.setValue(width);
            sliderHeight.setValue(height);
            int refresh = Integer.parseInt(tfRefreshRate.getText());
            sliderRefreshRate.setValue(refresh);
            int xOffset = Integer.parseInt(tfXOffset.getText());
            sliderXOffset.setValue(xOffset);
            int yOffset = Integer.parseInt(tfYOffset.getText());
            sliderYOffset.setValue(yOffset);
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            String dllName = "MagWrapper.dll";
            InputStream in = MagnifierGUI.class.getResourceAsStream("/" + dllName);
            if (in == null) {
                throw new FileNotFoundException("DLL not found from jar: " + dllName);
            }
            File tempDll = Files.createTempFile("temp_", "_" + dllName).toFile();
            tempDll.deleteOnExit();
            try (OutputStream out = new FileOutputStream(tempDll)) {
                byte[] buffer = new byte[1024];
                int readBytes;
                while ((readBytes = in.read(buffer)) != -1) {
                    out.write(buffer, 0, readBytes);
                }
            }
            System.load(tempDll.getAbsolutePath());
            EventQueue.invokeLater(() -> {
                MagnifierGUI gui = new MagnifierGUI();
                gui.setVisible(true);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class OffsetOverlay extends JWindow {
    private int shapeWidth;
    private int shapeHeight;
    private int xOffset;
    private int yOffset;
    private boolean circular;

    public OffsetOverlay(int shapeWidth, int shapeHeight, int xOffset, int yOffset, boolean circular) {
        this.shapeWidth = shapeWidth;
        this.shapeHeight = shapeHeight;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.circular = circular;
        setBackground(new Color(0, 0, 0, 0));
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(0, 0, screenSize.width, screenSize.height);
        setAlwaysOnTop(true);
    }

    public void updateSettings(int shapeWidth, int shapeHeight, int xOffset, int yOffset, boolean circular) {
        this.shapeWidth = shapeWidth;
        this.shapeHeight = shapeHeight;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.circular = circular;
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.RED);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int centerX = screenSize.width / 2 + xOffset;
        int centerY = screenSize.height / 2 + yOffset;
        int x = centerX - shapeWidth / 2;
        int y = centerY - shapeHeight / 2;
        if (circular) {
            g2d.drawOval(x, y, shapeWidth, shapeHeight);
        } else {
            g2d.drawRect(x, y, shapeWidth, shapeHeight);
        }
        g2d.drawLine(centerX - 10, centerY, centerX + 10, centerY);
        g2d.drawLine(centerX, centerY - 10, centerX, centerY + 10);
    }
}
