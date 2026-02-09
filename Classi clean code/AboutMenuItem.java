package com.bbn.openmap.gui.menu;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.bbn.openmap.Environment;
import com.bbn.openmap.MapBean;

/**
 * Classe per visualizzare le informazioni "About" di OpenMap.
 */
public class AboutMenuItem extends JMenuItem implements ActionListener {
    protected JDialog aboutBox = null;

    public AboutMenuItem() {
        super("About");

        // Breve spiegazione della scelta 't'
        setMnemonic('t');
        addActionListener(this);
    }

    public void actionPerformed(ActionEvent event) {
        initializeAboutBox();
        aboutBox.setVisible(true);
    }

    private void initializeAboutBox() {
        if (aboutBox == null) {
            aboutBox = createAboutBox();
            setupLayout();
        }
    }

    protected JDialog createAboutBox() {
        String title = "About " + Environment.get(Environment.Title);
        Container parent = getTopLevelAncestor();

        if (parent instanceof Frame) {
            return new JDialog((Frame) parent, title, true);
        }

        JDialog aboutDialog = new JDialog();
        aboutDialog.setTitle(title);
        return aboutDialog;
    }

    private void setupLayout() {
        Container contentPane = aboutBox.getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(createCopyrightInfo(), BorderLayout.CENTER);
        contentPane.add(createAboutControlPanel(), BorderLayout.SOUTH);
        aboutBox.pack();
    }

    protected JComponent createCopyrightInfo() {
        String copyrightInformation = buildCopyrightText();
        return createScrollingTextArea(copyrightInformation);
    }

    private String buildCopyrightText() {
        StringBuffer sb = new StringBuffer(MapBean.getCopyrightMessage())
                .append(System.lineSeparator())
                .append(System.lineSeparator())
                .append("Version ")
                .append(Environment.get(Environment.Version));

        String buildDate = Environment.get(Environment.BuildDate);
        if (buildDate != null) {
            sb.append(System.lineSeparator())
                    .append("Build ")
                    .append(buildDate);
        }
        return sb.toString();
    }

    private JScrollPane createScrollingTextArea(String content) {
        JTextArea viewer = new JTextArea(content);
        viewer.setEditable(false);
        return new JScrollPane(viewer);
    }

    protected Component createAboutControlPanel() {
        JButton okButton = new JButton("OK");
        Box box = Box.createHorizontalBox();
        box.add(okButton);
        okButton.addActionListener(e -> closeAboutDialog());
        return box;
    }

    private void closeAboutDialog() {
        if (aboutBox != null) {
            aboutBox.setVisible(false);
        }
    }

}