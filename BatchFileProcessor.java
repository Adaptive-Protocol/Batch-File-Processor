// Project: Batch File Processor
// Author: Joe PC
// Date: May 3, 2026
// Description: Bulk file rename tool with a Swing GUI that supports
// rename, renumber, extension change, selection, and move-to-deletion
// operations on files in a user-selected folder.

package fnl8;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class BatchFileProcessor extends JFrame {

    // Data state attributes
    private File currentFolder;
    private DefaultListModel<File> fileListModel;
    private Properties appProperties;

    // Folder bar widgets
    private JTextField folderPathField;
    private JButton browseButton;
    private JButton refreshButton;

    // File list widgets
    private JList<File> fileList;
    private JScrollPane fileListScrollPane;

    // Selection control widgets
    private JButton selectAllButton;
    private JTextField patternField;
    private JButton selectByPatternButton;

    // Rename and renumber widgets
    private JTextField filterField;
    private JTextField baseNameField;
    private JComboBox<String> sequenceTypeBox;
    private JButton renameButton;
    private JButton massOverwriteButton;
    private JButton sequenceChangerButton;

    // Extension change widgets
    private JTextField fromExtensionField;
    private JTextField toExtensionField;
    private JButton changeExtensionButton;

    // Move-to-deletion widgets
    private JButton deleteButton;

    // Progress bar widget
    private JProgressBar progressBar;

    // Status bar widgets
    private JLabel statusLabel;
    private JPanel statusPanel;
    private Timer statusRevertTimer;

    // Panel containers
    private JPanel folderBarPanel;
    private JPanel selectionPanel;
    private JPanel renamePanel;
    private JPanel extensionPanel;
    private JPanel deletionPanel;

    // Default constructor; builds the GUI with no preloaded folder
    public BatchFileProcessor() {
        appProperties = new Properties();
        fileListModel = new DefaultListModel<>();
        loadProperties();
        buildGUI();
        if (currentFolder != null) {
            if (currentFolder.isDirectory()) {
                folderPathField.setText(currentFolder.getAbsolutePath());
                populateFileList();
            } else {
                currentFolder = null;
            }
        }
    }

    // Constructor that builds the GUI with an initial folder path
    public BatchFileProcessor(String folderPath) {
        appProperties = new Properties();
        fileListModel = new DefaultListModel<>();
        File f = new File(folderPath);
        if (f.isDirectory()) {
            currentFolder = f;
        }
        buildGUI();
        if (currentFolder != null) {
            folderPathField.setText(currentFolder.getAbsolutePath());
            populateFileList();
        }
    }

    // Returns the operable folder
    public File getCurrentFolder() {
        return currentFolder;
    }

    // Sets the operable folder
    public void setCurrentFolder(File currentFolder) {
        this.currentFolder = currentFolder;
    }

    // Returns the file list model backing the GUI display
    public DefaultListModel<File> getFileListModel() {
        return fileListModel;
    }

    // Sets the file list model
    public void setFileListModel(DefaultListModel<File> fileListModel) {
        this.fileListModel = fileListModel;
    }

    // Returns the application properties
    public Properties getAppProperties() {
        return appProperties;
    }

    // Sets the application properties
    public void setAppProperties(Properties appProperties) {
        this.appProperties = appProperties;
    }

    // Assembles the GUI components and lays them out
    private void buildGUI() {
        setTitle("Batch File Processor");
        setSize(950, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Folder bar
        folderBarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        folderBarPanel.setBorder(BorderFactory.createTitledBorder("Folder"));
        folderPathField = new JTextField(40);
        folderPathField.setEditable(false);
        browseButton = new JButton("Browse");
        refreshButton = new JButton("Refresh");
        folderBarPanel.add(new JLabel("Path:"));
        folderBarPanel.add(folderPathField);
        folderBarPanel.add(browseButton);
        folderBarPanel.add(refreshButton);
        add(folderBarPanel, BorderLayout.NORTH);

        // File list with name-only renderer
        fileList = new JList<>(fileListModel);
        fileList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        fileList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof File) {
                    setText(((File) value).getName());
                }
                return this;
            }
        });
        fileListScrollPane = new JScrollPane(fileList);
        add(fileListScrollPane, BorderLayout.CENTER);

        // South container holds the four control panels stacked vertically
        JPanel southContainer = new JPanel();
        southContainer.setLayout(new BoxLayout(southContainer, BoxLayout.Y_AXIS));

        // Selection panel
        selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectionPanel.setBorder(BorderFactory.createTitledBorder("Selection"));
        selectAllButton = new JButton("Select All");
        patternField = new JTextField(15);
        selectByPatternButton = new JButton("Select by Pattern");
        selectionPanel.add(selectAllButton);
        selectionPanel.add(selectByPatternButton);
        selectionPanel.add(new JLabel("Pattern:"));
        selectionPanel.add(patternField);
        southContainer.add(selectionPanel);

        // Rename and renumber panel (shared sequence-type dropdown)
        renamePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        renamePanel.setBorder(BorderFactory.createTitledBorder("Rename and Renumber"));
        filterField = new JTextField(8);
        baseNameField = new JTextField(10);
        sequenceTypeBox = new JComboBox<>(new String[]{"Numerical", "Alphabetical"});
        renameButton = new JButton("Rename");
        massOverwriteButton = new JButton("Mass Overwrite");
        sequenceChangerButton = new JButton("Sequence Changer");
        renamePanel.add(new JLabel("Filter:"));
        renamePanel.add(filterField);
        renamePanel.add(new JLabel("Base Name:"));
        renamePanel.add(baseNameField);
        renamePanel.add(new JLabel("Type:"));
        renamePanel.add(sequenceTypeBox);
        renamePanel.add(renameButton);
        renamePanel.add(massOverwriteButton);
        renamePanel.add(sequenceChangerButton);
        southContainer.add(renamePanel);

        // Extension panel
        extensionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        extensionPanel.setBorder(BorderFactory.createTitledBorder("Rename File Extension"));
        fromExtensionField = new JTextField(6);
        toExtensionField = new JTextField(6);
        changeExtensionButton = new JButton("Change Extension");
        extensionPanel.add(changeExtensionButton);
        extensionPanel.add(new JLabel("From:"));
        extensionPanel.add(fromExtensionField);
        extensionPanel.add(new JLabel("To:"));
        extensionPanel.add(toExtensionField);
        southContainer.add(extensionPanel);

        // Deletion panel
        deletionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        deletionPanel.setBorder(BorderFactory.createTitledBorder("Delete Files"));
        deleteButton = new JButton("Delete");
        deletionPanel.add(deleteButton);
        southContainer.add(deletionPanel);

        // Status bar between deletion panel and progress bar
        statusLabel = new JLabel("No folder selected");
        statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        statusPanel.add(statusLabel);
        southContainer.add(statusPanel);

        // Progress bar reserved at the bottom for operation status
        progressBar = new JProgressBar();
        southContainer.add(progressBar);

        add(southContainer, BorderLayout.SOUTH);

        // Wire button listeners to action methods
        browseButton.addActionListener(e -> browseForFolder());
        refreshButton.addActionListener(e -> refreshFileList());
        selectAllButton.addActionListener(e -> selectAll());
        selectByPatternButton.addActionListener(e -> selectByPattern());
        renameButton.addActionListener(e -> renameSelected());
        massOverwriteButton.addActionListener(e -> massOverwrite());
        sequenceChangerButton.addActionListener(e -> changeSequence());
        changeExtensionButton.addActionListener(e -> changeExtension());
        deleteButton.addActionListener(e -> deleteSelected());

        // List selection listener for live status bar updates
        fileList.addListSelectionListener(e -> updateStatusBar());

        // Tooltips for buttons
        browseButton.setToolTipText("Select a folder");
        refreshButton.setToolTipText("Refresh the current folder");
        selectAllButton.setToolTipText("Select all files");
        selectByPatternButton.setToolTipText("Search only for files with defined text");
        renameButton.setToolTipText("Rename the selected file(s)");
        massOverwriteButton.setToolTipText("Overwrite every file with newly defined text");
        sequenceChangerButton.setToolTipText("Give the selected files a new sequence");
        changeExtensionButton.setToolTipText("Rename the file extension");
        deleteButton.setToolTipText(
                "Sends selected files to an output folder for deletion");
    }

    // Opens a chooser to select the operable folder
    private void browseForFolder() {
        JFileChooser chooser = new JFileChooser();
        if (currentFolder != null) {
            chooser.setCurrentDirectory(currentFolder);
        }
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            currentFolder = chooser.getSelectedFile();
            folderPathField.setText(currentFolder.getAbsolutePath());
            populateFileList();
            saveProperties();
        }
    }

    // Reloads the file list from the current folder
    private void refreshFileList() {
        if (currentFolder == null) {
            JOptionPane.showMessageDialog(this, "No folder selected.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!currentFolder.isDirectory()) {
            JOptionPane.showMessageDialog(this, "The current folder no longer exists.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        populateFileList();
    }

    // Loads the saved properties from disk
    private void loadProperties() {
        File propsFile = new File(System.getProperty("user.home"),
                "batchfileprocessor.properties");
        if (!propsFile.exists()) {
            return;
        }
        try (FileInputStream fis = new FileInputStream(propsFile)) {
            appProperties.load(fis);
            String last = appProperties.getProperty("lastFolder");
            if (last != null) {
                File f = new File(last);
                if (f.isDirectory()) {
                    currentFolder = f;
                }
            }
        } catch (IOException ex) {
            // No-op: first run or unreadable file
        }
    }

    // Saves the current folder selection to disk
    private void saveProperties() {
        if (currentFolder != null) {
            appProperties.setProperty("lastFolder", currentFolder.getAbsolutePath());
        }
        File propsFile = new File(System.getProperty("user.home"),
                "batchfileprocessor.properties");
        try (FileOutputStream fos = new FileOutputStream(propsFile)) {
            appProperties.store(fos, "Batch File Processor settings");
        } catch (IOException ex) {
            // No-op: cannot save settings
        }
    }

    // Populates the file list, excluding the deletion folder from display
    private void populateFileList() {
        fileListModel.clear();
        if (currentFolder == null || !currentFolder.isDirectory()) {
            return;
        }
        File[] files = currentFolder.listFiles();
        if (files == null) {
            return;
        }
        for (File f : files) {
            if (f.isFile()) {
                fileListModel.addElement(f);
            }
        }
        updateStatusBar();
    }

    // Selects all files in the list
    private void selectAll() {
        if (fileListModel.isEmpty()) {
            JOptionPane.showMessageDialog(this, "The file list is empty.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        fileList.setSelectionInterval(0, fileListModel.getSize() - 1);
    }

    // Selects files whose names contain the typed substring
    private void selectByPattern() {
        String pattern = patternField.getText();
        if (pattern.isEmpty()) {
            JOptionPane.showMessageDialog(this, "The pattern field is empty.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (fileListModel.isEmpty()) {
            JOptionPane.showMessageDialog(this, "The file list is empty.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        fileList.clearSelection();
        List<Integer> matches = new ArrayList<>();
        for (int i = 0; i < fileListModel.getSize(); i++) {
            File f = fileListModel.get(i);
            if (f.getName().contains(pattern)) {
                matches.add(i);
            }
        }
        int[] indices = new int[matches.size()];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = matches.get(i);
        }
        fileList.setSelectedIndices(indices);
    }

    // Renames selected files using base name and sequence type, with filter applied
    private void renameSelected() {
        if (currentFolder == null) {
            JOptionPane.showMessageDialog(this, "No folder selected.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        List<File> selected = fileList.getSelectedValuesList();
        if (selected.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No files selected.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String baseName = baseNameField.getText();
        if (baseName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "The base name field is empty.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String filter = filterField.getText();
        String sequenceType = (String) sequenceTypeBox.getSelectedItem();

        List<File> targets = new ArrayList<>();
        for (File f : selected) {
            if (filter.isEmpty() || f.getName().contains(filter)) {
                targets.add(f);
            }
        }
        if (targets.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No selected files match the filter.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!confirmAction("Rename " + targets.size() + " file(s)?")) {
            return;
        }

        setBusy(true);
        List<String> skipped = new ArrayList<>();
        int seqIndex = 0;
        for (File f : targets) {
            String name = f.getName();
            int dot = name.lastIndexOf('.');
            String ext = (dot > 0 && dot < name.length() - 1) ? name.substring(dot + 1) : "";
            String suffix = sequenceType.equals("Numerical")
                    ? String.valueOf(seqIndex + 1)
                    : generateAlphabeticalSequence(seqIndex);
            String newName = baseName + suffix + (ext.isEmpty() ? "" : "." + ext);
            File target = new File(f.getParentFile(), newName);
            if (target.exists() && !target.equals(f)) {
                skipped.add(name);
            } else if (!f.renameTo(target)) {
                skipped.add(name);
            }
            seqIndex++;
        }
        setBusy(false);
        populateFileList();
        if (!skipped.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Skipped " + skipped.size() + " file(s) due to collision: " + skipped,
                    "Skipped Files", JOptionPane.INFORMATION_MESSAGE);
        }
        showActionResult("Renamed " + (targets.size() - skipped.size()) + " files.");
        fileList.requestFocusInWindow();
    }

    // Renames every file in the folder, bypassing the filter as a mass overwrite
    private void massOverwrite() {
        if (currentFolder == null) {
            JOptionPane.showMessageDialog(this, "No folder selected.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String baseName = baseNameField.getText();
        if (baseName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "The base name field is empty.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String sequenceType = (String) sequenceTypeBox.getSelectedItem();

        File[] allFiles = currentFolder.listFiles();
        if (allFiles == null) {
            return;
        }
        List<File> targets = new ArrayList<>();
        for (File f : allFiles) {
            if (f.isFile()) {
                targets.add(f);
            }
        }
        if (targets.isEmpty()) {
            JOptionPane.showMessageDialog(this, "The folder contains no files.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!confirmAction("Mass overwrite " + targets.size()
                + " file(s) in the entire folder?")) {
            return;
        }

        setBusy(true);
        List<String> skipped = new ArrayList<>();
        int seqIndex = 0;
        for (File f : targets) {
            String name = f.getName();
            int dot = name.lastIndexOf('.');
            String ext = (dot > 0 && dot < name.length() - 1) ? name.substring(dot + 1) : "";
            String suffix = sequenceType.equals("Numerical")
                    ? String.valueOf(seqIndex + 1)
                    : generateAlphabeticalSequence(seqIndex);
            String newName = baseName + suffix + (ext.isEmpty() ? "" : "." + ext);
            File target = new File(f.getParentFile(), newName);
            if (target.exists() && !target.equals(f)) {
                skipped.add(name);
            } else if (!f.renameTo(target)) {
                skipped.add(name);
            }
            seqIndex++;
        }
        setBusy(false);
        populateFileList();
        if (!skipped.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Skipped " + skipped.size() + " file(s) due to collision: " + skipped,
                    "Skipped Files", JOptionPane.INFORMATION_MESSAGE);
        }
        showActionResult("Overwrote " + (targets.size() - skipped.size()) + " files.");
        fileList.requestFocusInWindow();
    }

    // Changes the sequence of selected files using sequence type, preserving base names
    private void changeSequence() {
        if (currentFolder == null) {
            JOptionPane.showMessageDialog(this, "No folder selected.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        List<File> selected = fileList.getSelectedValuesList();
        if (selected.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No files selected.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String sequenceType = (String) sequenceTypeBox.getSelectedItem();
        if (!confirmAction("Change sequence for " + selected.size() + " file(s)?")) {
            return;
        }

        setBusy(true);
        List<String> skipped = new ArrayList<>();
        int seqIndex = 0;
        for (File f : selected) {
            String name = f.getName();
            int dot = name.lastIndexOf('.');
            String stem = (dot > 0) ? name.substring(0, dot) : name;
            String ext = (dot > 0 && dot < name.length() - 1) ? name.substring(dot + 1) : "";
            // Strip trailing pure digits or trailing letter with optional digit prefix
            String strippedStem = stem.replaceAll("\\d+$|\\d*[a-zA-Z]$", "");
            String suffix = sequenceType.equals("Numerical")
                    ? String.valueOf(seqIndex + 1)
                    : generateAlphabeticalSequence(seqIndex);
            String newName = strippedStem + suffix + (ext.isEmpty() ? "" : "." + ext);
            File target = new File(f.getParentFile(), newName);
            if (target.exists() && !target.equals(f)) {
                skipped.add(name);
            } else if (!f.renameTo(target)) {
                skipped.add(name);
            }
            seqIndex++;
        }
        setBusy(false);
        populateFileList();
        if (!skipped.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Skipped " + skipped.size() + " file(s) due to collision: " + skipped,
                    "Skipped Files", JOptionPane.INFORMATION_MESSAGE);
        }
        showActionResult("Changed sequence on " + (selected.size() - skipped.size()) + " files.");
        fileList.requestFocusInWindow();
    }

    // Returns the alphabetical label for an index, wrapping past z with numeric prefix
    private String generateAlphabeticalSequence(int index) {
        int letterIndex = index % 26;
        int prefix = index / 26;
        char letter = (char) ('a' + letterIndex);
        if (prefix == 0) {
            return String.valueOf(letter);
        }
        return (prefix + 1) + String.valueOf(letter);
    }

    // Changes file extensions from "From" to "To" for selected matching files
    private void changeExtension() {
        if (currentFolder == null) {
            JOptionPane.showMessageDialog(this, "No folder selected.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        List<File> selected = fileList.getSelectedValuesList();
        if (selected.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No files selected.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String fromExt = fromExtensionField.getText();
        String toExt = toExtensionField.getText();
        if (fromExt.isEmpty() || toExt.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "The From and To extension fields are required.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<File> targets = new ArrayList<>();
        for (File f : selected) {
            String name = f.getName();
            int dot = name.lastIndexOf('.');
            if (dot > 0 && dot < name.length() - 1) {
                String ext = name.substring(dot + 1);
                if (ext.equals(fromExt)) {
                    targets.add(f);
                }
            }
        }
        if (targets.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No selected files have extension '" + fromExt + "'.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!confirmAction("Change extension on " + targets.size() + " file(s)?")) {
            return;
        }

        setBusy(true);
        List<String> skipped = new ArrayList<>();
        for (File f : targets) {
            String name = f.getName();
            int dot = name.lastIndexOf('.');
            String stem = name.substring(0, dot);
            String newName = stem + "." + toExt;
            File target = new File(f.getParentFile(), newName);
            if (target.exists() && !target.equals(f)) {
                skipped.add(name);
            } else if (!f.renameTo(target)) {
                skipped.add(name);
            }
        }
        setBusy(false);
        populateFileList();
        if (!skipped.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Skipped " + skipped.size() + " file(s) due to collision: " + skipped,
                    "Skipped Files", JOptionPane.INFORMATION_MESSAGE);
        }
        showActionResult("Changed extension on " + (targets.size() - skipped.size()) + " files.");
        fileList.requestFocusInWindow();
    }

    // Moves selected files into the "Files for Deletion" folder, creating it if needed
    private void deleteSelected() {
        if (currentFolder == null) {
            JOptionPane.showMessageDialog(this, "No folder selected.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        List<File> selected = fileList.getSelectedValuesList();
        if (selected.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No files selected.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!confirmAction("Move " + selected.size()
                + " file(s) to the 'Files for Deletion' folder?")) {
            return;
        }

        File deletionFolder = new File(currentFolder, "Files for Deletion");
        if (!deletionFolder.exists()) {
            if (!deletionFolder.mkdir()) {
                JOptionPane.showMessageDialog(this,
                        "Could not create the 'Files for Deletion' folder.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else if (!deletionFolder.isDirectory()) {
            JOptionPane.showMessageDialog(this,
                    "A non-folder item named 'Files for Deletion' already exists.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        setBusy(true);
        List<String> skipped = new ArrayList<>();
        for (File f : selected) {
            File target = new File(deletionFolder, f.getName());
            if (target.exists()) {
                skipped.add(f.getName());
            } else if (!f.renameTo(target)) {
                skipped.add(f.getName());
            }
        }
        setBusy(false);
        populateFileList();
        if (!skipped.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Skipped " + skipped.size() + " file(s): " + skipped,
                    "Skipped Files", JOptionPane.INFORMATION_MESSAGE);
        }
        showActionResult("Sent " + (selected.size() - skipped.size())
                + " files to deletion folder.");
        fileList.requestFocusInWindow();
    }

    // Toggles the progress bar between idle and busy states
    private void setBusy(boolean busy) {
        progressBar.setIndeterminate(busy);
    }

    // Updates the status bar to show current folder and selection counts
    private void updateStatusBar() {
        if (currentFolder == null) {
            statusLabel.setText("No folder selected");
            return;
        }
        int total = fileListModel.getSize();
        int selected = fileList.getSelectedIndices().length;
        statusLabel.setText(total + " files in folder, " + selected + " selected");
    }

    // Shows a temporary action result in the status bar that reverts after 3 seconds
    private void showActionResult(String message) {
        statusLabel.setText(message);
        if (statusRevertTimer != null && statusRevertTimer.isRunning()) {
            statusRevertTimer.stop();
        }
        statusRevertTimer = new Timer(3000, e -> updateStatusBar());
        statusRevertTimer.setRepeats(false);
        statusRevertTimer.start();
    }

    // Displays a confirmation prompt and returns the user's decision
    private boolean confirmAction(String message) {
        int response = JOptionPane.showConfirmDialog(this, message, "Confirm Action",
                JOptionPane.YES_NO_OPTION);
        return response == JOptionPane.YES_OPTION;
    }

    // Application entry point
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BatchFileProcessor app = new BatchFileProcessor();
            app.setVisible(true);
            app.browseButton.requestFocusInWindow();
        });
    }
}
