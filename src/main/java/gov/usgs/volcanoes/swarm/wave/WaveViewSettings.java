package gov.usgs.volcanoes.swarm.wave;

import gov.usgs.volcanoes.core.configfile.ConfigFile;
import gov.usgs.volcanoes.core.math.Butterworth;
import gov.usgs.volcanoes.core.util.StringUtils;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Wave View Settings.
 * 
 * @author Dan Cervelli
 */

public class WaveViewSettings {
  private static final String DEFAULTS_FILENAME = "WaveDefaults.config";

  public enum ViewType {
    WAVE("W"), SPECTRA("S"), SPECTROGRAM("G"), PARTICLE_MOTION("M");

    public String code;

    private ViewType(String c) {
      code = c;
    }

    /**
     * Get view type from String.
     * 
     * @param c S for Spectra, G for Specgrogram. Returns Wave otherwise.
     * @return view type enum (e.g. Spectra, Spectrogram, Wave)
     */
    public static ViewType fromString(String c) {
      if (c.equals("S")) {
        return SPECTRA;
      } else if (c.equals("G")) {
        return SPECTROGRAM;
      } else if (c.equals("M")) {
        return PARTICLE_MOTION;
      } else {
        return WAVE;
      }
    }
  }

  public boolean filterOn;
  public boolean zeroPhaseShift;
  public boolean autoScaleAmp;
  public boolean autoScaleAmpMemory;
  public boolean autoScalePower;
  public boolean autoScalePowerMemory;
  public boolean useUnits;
  public boolean logPower;
  public boolean logFreq;
  public boolean removeBias;

  public double maxAmp;
  public double minAmp;
  public double minPower;
  public double maxPower;
  public double minFreq;
  public double maxFreq;
  public double spectrogramOverlap;
  public double binSize;

  public int nfft;

  public WaveViewPanel view;
  public WaveViewSettingsToolbar toolbar;
  public ViewType viewType;
  public Butterworth filter;
  
  public boolean useAlternateOrientationCode = false;
  public String alternateOrientationCode = "Z12";
  public boolean pickEnabled = false;

  private static WaveViewSettings DEFAULT_WAVE_VIEW_SETTINGS;

  static {
    DEFAULT_WAVE_VIEW_SETTINGS = new WaveViewSettings();
    DEFAULT_WAVE_VIEW_SETTINGS.viewType = ViewType.WAVE;
    DEFAULT_WAVE_VIEW_SETTINGS.removeBias = true;
    DEFAULT_WAVE_VIEW_SETTINGS.autoScaleAmp = true;
    DEFAULT_WAVE_VIEW_SETTINGS.autoScaleAmpMemory = true;
    DEFAULT_WAVE_VIEW_SETTINGS.maxAmp = 1000;
    DEFAULT_WAVE_VIEW_SETTINGS.minAmp = -1000;
    DEFAULT_WAVE_VIEW_SETTINGS.autoScalePower = false;
    DEFAULT_WAVE_VIEW_SETTINGS.autoScalePowerMemory = true;
    DEFAULT_WAVE_VIEW_SETTINGS.minPower = 20;
    DEFAULT_WAVE_VIEW_SETTINGS.maxPower = 120;
    DEFAULT_WAVE_VIEW_SETTINGS.useUnits = true;
    DEFAULT_WAVE_VIEW_SETTINGS.logPower = true;
    DEFAULT_WAVE_VIEW_SETTINGS.logFreq = true;
    DEFAULT_WAVE_VIEW_SETTINGS.spectrogramOverlap = 0.859375;
    DEFAULT_WAVE_VIEW_SETTINGS.minFreq = 0;
    DEFAULT_WAVE_VIEW_SETTINGS.maxFreq = 25;
    DEFAULT_WAVE_VIEW_SETTINGS.binSize = 2;
    DEFAULT_WAVE_VIEW_SETTINGS.nfft = 0; // Zero means automatic
    DEFAULT_WAVE_VIEW_SETTINGS.filter = new Butterworth();
    DEFAULT_WAVE_VIEW_SETTINGS.filterOn = false;
    DEFAULT_WAVE_VIEW_SETTINGS.zeroPhaseShift = true;
    DEFAULT_WAVE_VIEW_SETTINGS.useAlternateOrientationCode = false;
    DEFAULT_WAVE_VIEW_SETTINGS.alternateOrientationCode = "Z12";

    List<String> candidateNames = new LinkedList<String>();
    candidateNames.add(DEFAULTS_FILENAME);
    candidateNames.add(System.getProperty("user.home") + File.separatorChar + DEFAULTS_FILENAME);
    String defaultsFile = ConfigFile.findConfig(candidateNames);
    if (defaultsFile == null) {
      defaultsFile = DEFAULTS_FILENAME;
    }

    ConfigFile cf = new ConfigFile(defaultsFile);
    if (cf.wasSuccessfullyRead()) {
      ConfigFile sub = cf.getSubConfig("default");
      DEFAULT_WAVE_VIEW_SETTINGS.set(sub);
    } else {
      DEFAULT_WAVE_VIEW_SETTINGS.save(cf, "default");
      cf.writeToFile(DEFAULTS_FILENAME);
    }
  }

  /**
   * Default Constructor.
   */
  public WaveViewSettings() {
    filter = new Butterworth();
    view = null;
    if (DEFAULT_WAVE_VIEW_SETTINGS != null) {
      copy(DEFAULT_WAVE_VIEW_SETTINGS);
    }
  }

  public WaveViewSettings(WaveViewSettings s) {
    copy(s);
  }

  /**
   * Deep copy WaveViewSettings.
   * @param s WaveViewSettings.
   */
  public void copy(WaveViewSettings s) {
    viewType = s.viewType;
    removeBias = s.removeBias;
    autoScaleAmp = s.autoScaleAmp;
    autoScaleAmpMemory = s.autoScaleAmpMemory;
    maxAmp = s.maxAmp;
    minAmp = s.minAmp;
    autoScalePowerMemory = s.autoScalePowerMemory;
    autoScalePower = s.autoScalePower;
    minPower = s.minPower;
    maxPower = s.maxPower;
    filter = new Butterworth(s.filter);
    useUnits = s.useUnits;
    minFreq = s.minFreq;
    maxFreq = s.maxFreq;
    binSize = s.binSize;
    nfft = s.nfft;
    spectrogramOverlap = s.spectrogramOverlap;
    logPower = s.logPower;
    logFreq = s.logFreq;
    zeroPhaseShift = s.zeroPhaseShift;
    filterOn = s.filterOn;
    useAlternateOrientationCode = s.useAlternateOrientationCode;
    alternateOrientationCode = s.alternateOrientationCode;
    pickEnabled = s.pickEnabled;
  }

  /**
   * Set configuration.
   * @param cf Configuration file.
   */
  public void set(ConfigFile cf) {
    viewType = ViewType.fromString(cf.getString("viewType"));
    filter.set(cf.getSubConfig("filter"));
    maxAmp = StringUtils.stringToDouble(cf.getString("maxAmp"), DEFAULT_WAVE_VIEW_SETTINGS.maxAmp);
    minAmp = StringUtils.stringToDouble(cf.getString("minAmp"), DEFAULT_WAVE_VIEW_SETTINGS.minAmp);
    maxPower =
        StringUtils.stringToDouble(cf.getString("maxPower"), DEFAULT_WAVE_VIEW_SETTINGS.maxPower);
    minPower =
        StringUtils.stringToDouble(cf.getString("minPower"), DEFAULT_WAVE_VIEW_SETTINGS.minPower);
    minFreq =
        StringUtils.stringToDouble(cf.getString("minFreq"), DEFAULT_WAVE_VIEW_SETTINGS.minFreq);
    maxFreq =
        StringUtils.stringToDouble(cf.getString("maxFreq"), DEFAULT_WAVE_VIEW_SETTINGS.maxFreq);
    spectrogramOverlap = StringUtils.stringToDouble(cf.getString("spectrogramOverlap"),
        DEFAULT_WAVE_VIEW_SETTINGS.spectrogramOverlap);

    removeBias = StringUtils.stringToBoolean(cf.getString("removeBias"),
        DEFAULT_WAVE_VIEW_SETTINGS.removeBias);
    filterOn =
        StringUtils.stringToBoolean(cf.getString("filterOn"), DEFAULT_WAVE_VIEW_SETTINGS.filterOn);
    zeroPhaseShift = StringUtils.stringToBoolean(cf.getString("zeroPhaseShift"),
        DEFAULT_WAVE_VIEW_SETTINGS.zeroPhaseShift);
    autoScaleAmp = StringUtils.stringToBoolean(cf.getString("autoScaleAmp"),
        DEFAULT_WAVE_VIEW_SETTINGS.autoScaleAmp);
    autoScaleAmpMemory = StringUtils.stringToBoolean(cf.getString("autoScaleAmpMemory"),
        DEFAULT_WAVE_VIEW_SETTINGS.autoScaleAmpMemory);
    autoScalePower = StringUtils.stringToBoolean(cf.getString("autoScalePower"),
        DEFAULT_WAVE_VIEW_SETTINGS.autoScalePower);
    autoScalePowerMemory = StringUtils.stringToBoolean(cf.getString("autoScalePowerMemory"),
        DEFAULT_WAVE_VIEW_SETTINGS.autoScalePowerMemory);
    useUnits =
        StringUtils.stringToBoolean(cf.getString("useUnits"), DEFAULT_WAVE_VIEW_SETTINGS.useUnits);
    logFreq =
        StringUtils.stringToBoolean(cf.getString("logFreq"), DEFAULT_WAVE_VIEW_SETTINGS.logFreq);
    logPower =
        StringUtils.stringToBoolean(cf.getString("logPower"), DEFAULT_WAVE_VIEW_SETTINGS.logPower);
    binSize =
        StringUtils.stringToDouble(cf.getString("binSize"), DEFAULT_WAVE_VIEW_SETTINGS.binSize);
    nfft = StringUtils.stringToInt(cf.getString("nfft"), DEFAULT_WAVE_VIEW_SETTINGS.nfft);
    useAlternateOrientationCode =
        StringUtils.stringToBoolean(cf.getString("useAlternateOrientationCode"),
            DEFAULT_WAVE_VIEW_SETTINGS.useAlternateOrientationCode);
    alternateOrientationCode = StringUtils.stringToString(cf.getString("alternateOrientationCode"),
        DEFAULT_WAVE_VIEW_SETTINGS.alternateOrientationCode);
  }

  /**
   * Save configuration file.
   * @param cf Configuration file.
   * @param prefix Configuration name prefix.
   */
  public void save(ConfigFile cf, String prefix) {
    cf.put(prefix + ".viewType", viewType.code);
    filter.save(cf, prefix + ".filter");
    cf.put(prefix + ".maxAmp", Double.toString(maxAmp));
    cf.put(prefix + ".minAmp", Double.toString(minAmp));
    cf.put(prefix + ".minPower", Double.toString(minPower));
    cf.put(prefix + ".maxPower", Double.toString(maxPower));
    cf.put(prefix + ".minFreq", Double.toString(minFreq));
    cf.put(prefix + ".maxFreq", Double.toString(maxFreq));
    cf.put(prefix + ".spectrogramOverlap", Double.toString(spectrogramOverlap));
    cf.put(prefix + ".removeBias", Boolean.toString(removeBias));
    cf.put(prefix + ".filterOn", Boolean.toString(filterOn));
    cf.put(prefix + ".zeroPhaseShift", Boolean.toString(zeroPhaseShift));
    cf.put(prefix + ".autoScaleAmp", Boolean.toString(autoScaleAmp));
    cf.put(prefix + ".autoScaleAmpMemory", Boolean.toString(autoScaleAmpMemory));
    cf.put(prefix + ".autoScalePower", Boolean.toString(autoScalePower));
    cf.put(prefix + ".autoScalePowerMemory", Boolean.toString(autoScalePowerMemory));
    cf.put(prefix + ".useUnits", Boolean.toString(useUnits));
    cf.put(prefix + ".logFreq", Boolean.toString(logFreq));
    cf.put(prefix + ".logPower", Boolean.toString(logPower));
    cf.put(prefix + ".binSize", Double.toString(binSize));
    cf.put(prefix + ".nfft", Integer.toString(nfft));
    cf.put(prefix + ".useAlternateOrientationCode", Boolean.toString(useAlternateOrientationCode));
    cf.put(prefix + ".alternateOrientationCode", alternateOrientationCode);
  }

  public void setType(ViewType t) {
    viewType = t;
    notifyView();
  }

  /**
   * Get cycle type based on view type.
   */
  public void cycleType() {
    switch (viewType) {
      case WAVE:
        viewType = ViewType.SPECTRA;
        break;
      case SPECTRA:
        viewType = ViewType.SPECTROGRAM;
        break;
      case SPECTROGRAM:
        viewType = ViewType.PARTICLE_MOTION;
        break;
      case PARTICLE_MOTION:
        viewType = ViewType.WAVE;
        break;
      default:
        break;
    }
    notifyView();
  }

  /**
   * Set cycle log settings.
   */
  public void cycleLogSettings() {

    if (logFreq == logPower) {
      logPower = !logPower;
    } else {
      logFreq = !logFreq;
    }

    notifyView();
  }

  public void toggleLogFreq() {
    logFreq = !logFreq;
    notifyView();
  }

  public void toggleLogPower() {
    logPower = !logPower;
    notifyView();
  }

  public void toggleFilter() {
    filterOn = !filterOn;
    notifyView();
  }

  /**
   * Reset view's auto scale memory setting.
   */
  public void resetAutoScaleMemory() {
    if (view != null) {
      view.resetAutoScaleMemory();
    }
  }

  /**
   * Adjust view's scale.
   * @param pct Scale percent.
   */
  public void adjustScale(double pct) {
    if (view != null) {
      view.adjustScale(pct);
    }
  }

  /**
   * Notify view of settings change.
   */
  public void notifyView() {
    if (view != null) {
      view.settingsChanged();
    }

    if (toolbar != null) {
      toolbar.settingsChanged();
    }
  }

}
