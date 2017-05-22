package gov.usgs.volcanoes.swarm.event;

import gov.usgs.plot.data.Wave;
import gov.usgs.volcanoes.core.quakeml.Event;
import gov.usgs.volcanoes.core.quakeml.EventSet;
import gov.usgs.volcanoes.core.quakeml.Pick;
import gov.usgs.volcanoes.core.time.J2kSec;
import gov.usgs.volcanoes.swarm.Swarm;
import gov.usgs.volcanoes.swarm.SwarmConfig;
import gov.usgs.volcanoes.swarm.data.SeismicDataSource;
import gov.usgs.volcanoes.swarm.wave.WaveClipboardFrame;
import gov.usgs.volcanoes.swarm.wave.WaveViewPanel;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Right click menu for picks.
 * 
 * @author Diana Norgaard
 */
public class PickMenuBar extends JMenuBar {

  private static final Logger LOGGER = LoggerFactory.getLogger(PickMenuBar.class);
  private static final long serialVersionUID = 8681764007165352268L;

  private PickSettingsDialog settingsDialog;
  private EventDialog eventDialog;
  private static PickMenuBar menuBar;
  private JMenu menu;
  
  /**
   * Constructor.
   */
  private PickMenuBar() {
    super();
    menu = new JMenu("Pick Menu");
    this.add(menu);
    this.setLayout(new GridLayout(1, 1));
    settingsDialog = PickSettingsDialog.getInstance();
    eventDialog = EventDialog.getInstance();
    createMenu();
  }
  
  /**
   * Get instance of PickModeMenu.
   * @return pick mode menu
   */
  public static PickMenuBar getInstance() {
    if (menuBar == null) {
      menuBar = new PickMenuBar();
    }
    return menuBar;
  }

  /**
   * Create right click menu for pick.
   */
  private void createMenu() {
    createSettingsMenu();
    createEventMenu();
  }
 
  /**
   * Create import/export event menu items.
   */
  private void createEventMenu() {
    JMenuItem importMenu = new JMenuItem("Import...");
    importMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
    importMenu.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        openImportFileDialog();
      }
    });
    menu.add(importMenu);
    
    JMenuItem exportMenu = new JMenuItem("Export...");
    exportMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
    exportMenu.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        openEventDialog();
      }
    });
    menu.add(exportMenu);
  }
  
  /**
   * Open import file dialog.
   */
  private void openImportFileDialog() {
    JFileChooser chooser = new JFileChooser();
    chooser.setFileFilter(new FileNameExtensionFilter("QuakeML (.xml)", "xml"));
    chooser.setCurrentDirectory(new File(SwarmConfig.getInstance().lastPath));
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setMultiSelectionEnabled(false);
    chooser.setDialogTitle("Select QuakeML file...");
    int result = chooser.showOpenDialog(Swarm.getApplicationFrame());
    if (result == JFileChooser.APPROVE_OPTION) {
      File f = chooser.getSelectedFile();
      importFile(f);
      SwarmConfig.getInstance().lastPath = f.getParent();
    }
  }
  
  /**
   * Import event from file.
   * @param f file
   */
  private void importFile(File f) {
    try {
      EventSet eventSet = EventSet.parseQuakeml(new FileInputStream(f));
      if (eventSet.size() == 0) {
        JOptionPane.showMessageDialog(WaveClipboardFrame.getInstance(), "No events found in file.");
        return;
      }
      Event event;
      if (eventSet.size() > 1) { // Get user to decide which event to import
        HashMap<String, Event> eventMap = new HashMap<String, Event>();
        for (Event e : eventSet.values()) {
          String description = e.getDescription();
          if (description == null || description.equals("")) {
            description = e.publicId;
          }
          eventMap.put(description, e);
        }
        event = openEventChooser(eventMap);
      } else {
        event = eventSet.values().iterator().next();
      }
      importEvent(event);
    } catch (FileNotFoundException e) {
      LOGGER.warn(e.getMessage());
    } catch (IOException e) {
      LOGGER.warn(e.getMessage());
    } catch (ParserConfigurationException e) {
      LOGGER.warn(e.getMessage());
    } catch (SAXException e) {
      LOGGER.warn(e.getMessage());
    }
  }
  
  /**
   * Open chooser with list of event descriptions.
   * @param eventMap map of description to event
   * @return user selected event
   */
  private Event openEventChooser(HashMap<String, Event> eventMap) {
    String s = (String) JOptionPane.showInputDialog(WaveClipboardFrame.getInstance(),
        "Select event to import", "Import Event", JOptionPane.PLAIN_MESSAGE, null,
        eventMap.keySet().toArray(), eventMap.keySet().iterator().next());
    return eventMap.get(s);
  }
  
  /**
   * Import event into clipboard.
   * @param event event
   */
  private void importEvent(Event event) {
    // update event dialog 
    eventDialog.setDescription(event.getDescription());
    eventDialog.setEventType(event.getType());
    eventDialog.setEventTypeCertainty(event.getTypeCertainty());
    
    // get wave start and end times
    long firstPick = Long.MAX_VALUE;
    long lastPick = Long.MIN_VALUE;

    for (Pick pick : event.getPicks().values()) {
      firstPick = Math.min(pick.getTime(), firstPick);
      lastPick = Math.max(pick.getTime(), lastPick);
    }
    double waveStart = J2kSec.fromEpoch(firstPick) - 2;
    double waveEnd = J2kSec.fromEpoch(lastPick) + 2;

    // create wave view panels 
    HashMap<String, WaveViewPanel> panels = new HashMap<String, WaveViewPanel>();
    for (Pick pick : event.getPicks().values()) {
      String channel = pick.getChannel().replaceAll("\\$", " ").trim();
      WaveViewPanel wvp = panels.get(channel);
      if (wvp == null) {
        wvp = new WaveViewPanel();
        wvp.setChannel(channel);
        wvp.setStartTime(waveStart);
        wvp.setEndTime(waveEnd);
        boolean foundSource = false;
        for (SeismicDataSource source : SwarmConfig.getInstance().getSources().values()) {
          for (String ch : source.getChannels()) {
            if (ch.equals(channel)) {
              wvp.setDataSource(source);
              Wave wave = source.getWave(channel, waveStart, waveEnd);
              if (wave != null) {
                wvp.setWave(wave, waveStart, waveEnd);
                foundSource = true;
                break;
              }
            }
          }
          if (foundSource) {
            break;
          }
        }
        panels.put(channel, wvp);
        if (!foundSource) {
          JOptionPane.showMessageDialog(WaveClipboardFrame.getInstance(),
              "No data source found for channel: " + channel);
        }
      }
      String phaseHint = pick.getPhaseHint();
      PickMenu pickMenu = wvp.getPickMenu();
      if (phaseHint.equals("P")) {
        pickMenu.setP(pick);
        pickMenu.setPickChannelP(true);
      }
      if (phaseHint.equals("S")) {
        pickMenu.setS(pick);
        pickMenu.setPickChannelS(true);
      }
    }

    // add wave view panels to clipboard
    for (WaveViewPanel wvp : panels.values()) {
      WaveClipboardFrame.getInstance().addWave(wvp);
    }
  }
  
  /**
   * Open event dialog for export to file.
   */
  private void openEventDialog() {
    if (WaveClipboardFrame.getInstance().getWaves().isEmpty()) {
      String message = "Nothing to export!";
      JOptionPane.showMessageDialog(WaveClipboardFrame.getInstance(), message);
      return;
    }
    String message = "Every pick in the clipboard will be saved. Continue?";
    int result = JOptionPane.showConfirmDialog(WaveClipboardFrame.getInstance(), message, "Export",
        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
    if (result != JOptionPane.YES_OPTION) {
      return;
    }
    eventDialog.setVisible(true);    
  }
  
  /**
   * Create settings menu item.
   */
  private void createSettingsMenu() {
    JMenuItem settingsMenu = new JMenuItem("Settings");
    settingsMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
    settingsMenu.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        settingsDialog.setVisible(true);
      }
    });
    menu.add(settingsMenu);
  }
  
}