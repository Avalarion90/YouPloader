/* 
 * YouPloader Copyright (c) 2016 genuineparts (itsme@genuineparts.org)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 */
package at.becast.youploader.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

import at.becast.youploader.account.Account;
import at.becast.youploader.account.AccountManager;
import at.becast.youploader.account.AccountType;
import at.becast.youploader.database.SQLite;
import at.becast.youploader.gui.slider.SideBar;
import at.becast.youploader.gui.slider.SidebarSection;
import at.becast.youploader.settings.Settings;
import at.becast.youploader.templates.Item;
import at.becast.youploader.templates.Template;
import at.becast.youploader.templates.TemplateManager;
import at.becast.youploader.util.DesktopUtil;
import at.becast.youploader.util.GetVersion;
import at.becast.youploader.util.NativeJFileChooser;
import at.becast.youploader.util.VersionComparator;
import at.becast.youploader.youtube.Categories;
import at.becast.youploader.youtube.LicenseType;
import at.becast.youploader.youtube.SyndicationType;
import at.becast.youploader.youtube.VisibilityType;
import at.becast.youploader.youtube.data.Video;
import at.becast.youploader.youtube.data.VideoMetadata;
import at.becast.youploader.youtube.io.UploadManager;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import net.miginfocom.layout.CC;
import net.miginfocom.swing.MigLayout;
import javax.swing.JCheckBoxMenuItem;

/**
 *
 * @author genuineparts
 */

public class FrmMain extends JFrame implements IMainMenu {

	private static final long serialVersionUID = 6965358827253585528L;
	public static final String DB_FILE = System.getProperty("user.home") + "/YouPloader/data/data.db";
	public static final String APP_NAME = "YouPloader";
	public static final String VERSION = "0.5";
	public static final int DB_VERSION = 5;
	private static final String DEFAULT_WIDTH = "900";
	private static final String DEFAULT_HEIGHT = "580";
	private static final Logger LOG = LoggerFactory.getLogger(FrmMain.class);
	public static UploadManager UploadMgr;
	public static TemplateManager TemplateMgr;
	private static final ResourceBundle LANG = ResourceBundle.getBundle("lang", Locale.getDefault());
	private static Settings s;
	private static Boolean firstlaunch = false;
	private AccountManager accMng = AccountManager.getInstance();
	private Boolean tos;
	private JButton btnAddToQueue;
	private ModalDialog modal;
	private IMainMenu self;
	private JTextArea txtDescription;
	private JTabbedPane TabbedPane;
	private JComboBox<String> cmbFile;
	private JMenu mnuAcc;
	private JTextField txtTitle;
	private JCheckBoxMenuItem chckbxmntmCheckForUpdates;
	private JLabel lblTagslenght, lbltitlelenght, lblDesclenght;
	private JSpinner spinner;
	private JPanel QueuePanel;
	private JComboBox<Categories> cmbCategory;
	private JTextArea txtTags;
	private JComboBox<AccountType> cmbAccount;
	private SidebarSection ss1, ss2, ss3;
	public transient static HashMap<Integer, JMenuItem> _accounts = new HashMap<Integer, JMenuItem>();
	private int editItem = -1;

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String args[]) {
		if (args.length > 0 && args[0].equalsIgnoreCase("debug")) {
			LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
			// print logback's internal status
			StatusPrinter.print(lc);
		}

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			LOG.error("Look and Feel exception", e);
		}
		File dataDir = new File(System.getProperty("user.home") + "/YouPloader/data/");
		if (!dataDir.exists()) {
			LOG.info(APP_NAME + " " + VERSION + " first launch. Database folder not found.", FrmMain.class);
			dataDir.mkdirs();
			if (!SQLite.setup()) {
				JOptionPane.showMessageDialog(null,
						String.format(LANG.getString("frmMain.errordatabase.Message"),
								System.getProperty("user.home") + "/YouPloader/data/"),
						LANG.getString("frmMain.errordatabase.title"), JOptionPane.ERROR_MESSAGE);
			}
			firstlaunch = true;
		} else {
			SQLite.getInstance();
			try {
				if(SQLite.getVersion()<DB_VERSION){
					SQLite.close();
					SQLite.makeBackup();
					SQLite.getInstance();
					SQLite.update();
				}
			} catch (SQLException e) {
				LOG.info("Failed to get DB Version ", e);
			}
		}
		UploadMgr = UploadManager.getInstance();
		TemplateMgr = TemplateManager.getInstance();
		s = Settings.getInstance();
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new FrmMain().setVisible(true);
			}
		});
	}

	/**
	 * Creates new form frmMain
	 */
	public FrmMain() {
		LOG.info(APP_NAME + " " + VERSION + " starting.", FrmMain.class);
		self = this;
		this.tos = false;
		this.setMinimumSize(new Dimension(900, 580));
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				LOG.info(APP_NAME + " " + VERSION + " closing.", FrmMain.class);
				s.put("left", String.valueOf(getX()));
				s.put("top", String.valueOf(getY()));
				s.put("width", String.valueOf(getWidth()));
				s.put("height", String.valueOf(getHeight()));
				LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
				loggerContext.stop();
				e.getWindow().dispose();
			}
		});
		UploadMgr.setParent(this);
		initComponents();
		initMenuBar();
		loadAccounts();
		this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/yp.png")));
		try {
			loadQueue();
		} catch (SQLException | IOException e) {
			LOG.error("Error: ", e);
		}
		EditPanel edit = (EditPanel) ss1.contentPane;
		if (edit.getCmbTemplate().getModel().getSize() > 0) {
			edit.getCmbTemplate().setSelectedIndex(0);
		}
		this.setVisible(true);
		if (firstlaunch) {
			int n = JOptionPane.showConfirmDialog(null, LANG.getString("frmMain.initialAccount.Message"),
					LANG.getString("frmMain.initialAccount.title"), JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			if (n == JOptionPane.YES_OPTION) {
				mntmAddAccountActionPerformed();
			}
		}
		
		if(s.setting.get("notify_updates").equals("1")){
			checkUpdates();
		}
	}

	private void checkUpdates() {
		String gitVersion = GetVersion.get();
		VersionComparator v = new VersionComparator();
		int updateAvaiable = v.compare(gitVersion, VERSION);
		if(updateAvaiable > 0){
			LOG.info("Update {} avaiable!", gitVersion);
			int n = JOptionPane.showConfirmDialog(null, String.format(LANG.getString("frmMain.newVersion.Message"),gitVersion),
					LANG.getString("frmMain.newVersion.title"), JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			if (n == JOptionPane.YES_OPTION) {
				DesktopUtil.openBrowser("https://github.com/becast/youploader/releases");
			}
		}
		
	}

	/**
	 * 
	 */
	public void initComponents() {
		LOG.debug("init Components", FrmMain.class);
		int left = Integer.parseInt(s.get("left","0"));
		int top = Integer.parseInt(s.get("top","0"));
		int width = Integer.parseInt(s.get("width",DEFAULT_WIDTH));
		int height = Integer.parseInt(s.get("height", DEFAULT_HEIGHT));
		setBounds(left, top, width, height);
		TabbedPane = new JTabbedPane();
		cmbCategory = new JComboBox<Categories>();
		cmbCategory.setModel(new DefaultComboBoxModel<Categories>(Categories.values()));
		SideBar sideBar = new SideBar(SideBar.SideBarMode.TOP_LEVEL, true, 300, true);
		ss1 = new SidebarSection(sideBar, LANG.getString("frmMain.Sidebar.Settings"), new EditPanel(this), null);
		ss2 = new SidebarSection(sideBar, LANG.getString("frmMain.Sidebar.Playlists"), new PlaylistPanel(), null);
		ss3 = new SidebarSection(sideBar, LANG.getString("frmMain.Sidebar.Monetisation"), new MonetPanel(), null);
		sideBar.addSection(ss1, false);
		sideBar.addSection(ss2);
		sideBar.addSection(ss3);
		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		setTitle(APP_NAME + " " + VERSION);
		setName("frmMain");
		JPanel mainTab = new JPanel();
		JPanel panel = new JPanel();
		GroupLayout mainTabLayout = new GroupLayout(mainTab);
		mainTabLayout
				.setHorizontalGroup(mainTabLayout.createParallelGroup(Alignment.LEADING).addGroup(Alignment.TRAILING,
						mainTabLayout.createSequentialGroup()
								.addComponent(panel, GroupLayout.DEFAULT_SIZE, 469, Short.MAX_VALUE)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(sideBar, GroupLayout.DEFAULT_SIZE, 416, Short.MAX_VALUE)));
		mainTabLayout.setVerticalGroup(mainTabLayout.createParallelGroup(Alignment.LEADING)
				.addComponent(sideBar, GroupLayout.DEFAULT_SIZE, 473, Short.MAX_VALUE)
				.addComponent(panel, GroupLayout.DEFAULT_SIZE, 473, Short.MAX_VALUE));
		panel.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("2px"),
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("20px:grow"),
				FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,
				ColumnSpec.decode("23px"),
				ColumnSpec.decode("33px"),
				FormSpecs.UNRELATED_GAP_COLSPEC,
				ColumnSpec.decode("61px"),
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("24px"),
				ColumnSpec.decode("28px"),
				ColumnSpec.decode("40px"),
				ColumnSpec.decode("36px"),
				FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,
				ColumnSpec.decode("28px"),
				FormSpecs.LABEL_COMPONENT_GAP_COLSPEC,
				ColumnSpec.decode("58px"),},
			new RowSpec[] {
				RowSpec.decode("2px"),
				FormSpecs.RELATED_GAP_ROWSPEC,
				RowSpec.decode("14px"),
				RowSpec.decode("25px"),
				FormSpecs.RELATED_GAP_ROWSPEC,
				RowSpec.decode("14px"),
				RowSpec.decode("25px"),
				FormSpecs.LINE_GAP_ROWSPEC,
				RowSpec.decode("14px"),
				RowSpec.decode("25px"),
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				RowSpec.decode("64dlu:grow"),
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				RowSpec.decode("max(64dlu;default)"),
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				RowSpec.decode("25px"),
				FormSpecs.PARAGRAPH_GAP_ROWSPEC,
				RowSpec.decode("24px"),
				RowSpec.decode("23px"),}));

		lbltitlelenght = new JLabel("(0/100)");
		panel.add(lbltitlelenght, "14, 6, 3, 1, right, top");

		txtTitle = new JTextField();
		panel.add(txtTitle, "3, 7, 14, 1, fill, fill");
		txtTitle.setColumns(10);
		txtTitle.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				calcNotifies();
			}
		});

		JLabel lblCategory = new JLabel(LANG.getString("frmMain.Category"));
		panel.add(lblCategory, "3, 9, 4, 1, left, bottom");
		panel.add(cmbCategory, "3, 10, 14, 1, fill, fill");

		JLabel lblDescription = new JLabel(LANG.getString("frmMain.Description"));
		panel.add(lblDescription, "3, 12, 4, 1, left, bottom");

		lblDesclenght = new JLabel("(0/5000)");
		panel.add(lblDesclenght, "14, 12, 3, 1, right, bottom");

		JScrollPane DescriptionScrollPane = new JScrollPane();
		panel.add(DescriptionScrollPane, "3, 13, 14, 1, fill, fill");

		txtDescription = new JTextArea();
		txtDescription.setFont(new Font("SansSerif", Font.PLAIN, 13));
		DescriptionScrollPane.setViewportView(txtDescription);
		txtDescription.setWrapStyleWord(true);
		txtDescription.setLineWrap(true);
		txtDescription.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				calcNotifies();
			}
		});
		
		JLabel lblTags = new JLabel(LANG.getString("frmMain.Tags"));
		panel.add(lblTags, "3, 15, 4, 1, left, bottom");

		lblTagslenght = new JLabel("(0/500)");
		panel.add(lblTagslenght, "14, 15, 3, 1, right, top");

		JScrollPane TagScrollPane = new JScrollPane();
		panel.add(TagScrollPane, "3, 16, 14, 1, fill, fill");

		txtTags = new JTextArea();
		txtTags.setFont(new Font("SansSerif", Font.PLAIN, 13));
		TagScrollPane.setViewportView(txtTags);
		txtTags.setWrapStyleWord(true);
		txtTags.setLineWrap(true);
		txtTags.setBorder(BorderFactory.createEtchedBorder());
		txtTags.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				calcNotifies();
			}
		});

		JLabel lblAccount = new JLabel(LANG.getString("frmMain.Account"));
		panel.add(lblAccount, "3, 18, 4, 1, left, bottom");
		cmbAccount = new JComboBox<AccountType>();
		panel.add(cmbAccount, "3, 19, 14, 1, fill, fill");

		btnAddToQueue = new JButton(LANG.getString("frmMain.addtoQueue"));
		btnAddToQueue.setEnabled(false);
		panel.add(btnAddToQueue, "3, 21, 6, 1, fill, fill");
		btnAddToQueue.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				queueButton();
			}
		});
		JLabel lblSelectVideo = new JLabel();
		panel.add(lblSelectVideo, "3, 3, 4, 1, left, bottom");

		lblSelectVideo.setText(LANG.getString("frmMain.selectVideoFile"));
		cmbFile = new JComboBox<String>();
		panel.add(cmbFile, "3, 4, 14, 1, fill, fill");
		JButton btnSelectMovie = new JButton();
		btnSelectMovie.setToolTipText("Select Video File");
		panel.add(btnSelectMovie, "18, 4, center, top");
		btnSelectMovie.setIcon(new ImageIcon(getClass().getResource("/film_add.png")));

		JLabel lblTitle = new JLabel(LANG.getString("frmMain.Title"));
		panel.add(lblTitle, "3, 6, 4, 1, left, bottom");

		JButton btnReset = new JButton(LANG.getString("frmMain.Reset"));
		btnReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				resetEdit();
			}
		});
		panel.add(btnReset, "11, 21, 6, 1, fill, fill");
		btnSelectMovie.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				EditPanel edit = (EditPanel) ss1.contentPane;
				NativeJFileChooser chooser;
				if (edit.getTxtStartDir() != null && !edit.getTxtStartDir().equals("")) {
					chooser = new NativeJFileChooser(edit.getTxtStartDir().getText().trim());
				}else{
					chooser = new NativeJFileChooser();
				}
				int returnVal = chooser.showOpenDialog((Component) self);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					cmbFile.removeAllItems();
					cmbFile.addItem(chooser.getSelectedFile().getAbsolutePath().toString());
				}
			}
		});
		mainTab.setLayout(mainTabLayout);
		TabbedPane.addTab(LANG.getString("frmMain.Tabs.VideoSettings"), mainTab);

		GroupLayout layout = new GroupLayout(getContentPane());
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addContainerGap().addComponent(TabbedPane).addContainerGap()));
		layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING).addComponent(TabbedPane,
				GroupLayout.DEFAULT_SIZE, 501, Short.MAX_VALUE));

		JScrollPane TabQueues = new JScrollPane();
		QueuePanel = new JPanel();
		TabQueues.setViewportView(QueuePanel);
		getContentPane().setLayout(layout);
		QueuePanel.setLayout(new MigLayout("", "[875px,grow,fill]", "[][][][]"));
		JPanel buttonPanel = new JPanel();
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.fill = GridBagConstraints.BOTH;
		gbc_panel_2.gridx = 0;
		gbc_panel_2.gridy = 1;
		buttonPanel.setLayout(new FormLayout(
				new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
						FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC,
						FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
						FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC,
						FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
						FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC,
						ColumnSpec.decode("max(39dlu;default)"), FormSpecs.RELATED_GAP_COLSPEC,
						FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
						FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(92dlu;default)"),
						FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(46dlu;min)"),
						FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("left:max(22dlu;default)"),
						FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, },
				new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC,
						FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC,
						RowSpec.decode("max(0dlu;default)"), }));

		JButton btnStart = new JButton("Start");
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startUploads();
			}
		});
		buttonPanel.add(btnStart, "2, 4");

		JButton btnStop = new JButton("Stop");
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				UploadMgr.stop();
			}
		});
		buttonPanel.add(btnStop, "6, 4");

		JLabel lblUploads = new JLabel("Uploads:");
		buttonPanel.add(lblUploads, "18, 4, right, fill");

		JSlider slider = new JSlider();
		slider.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				JSlider s = (JSlider) evt.getSource();
				UploadMgr.setUploadlimit(s.getValue());
			}
		});
		slider.setMajorTickSpacing(1);
		slider.setMinorTickSpacing(1);
		slider.setMinimum(1);
		slider.setMaximum(5);
		slider.setValue(1);
		slider.setSnapToTicks(true);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		buttonPanel.add(slider, "20, 4, fill, fill");

		JLabel lblUploadSpeed = new JLabel("Upload Speed:");
		lblUploadSpeed.setHorizontalAlignment(SwingConstants.TRAILING);
		buttonPanel.add(lblUploadSpeed, "24, 4");

		spinner = new JSpinner();
		spinner.setModel(new SpinnerNumberModel(new Integer(0), new Integer(0), null, new Integer(10)));
		spinner.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				JSpinner s = (JSpinner) e.getSource();
				UploadMgr.setLimit(Integer.parseInt(s.getValue().toString()));
			}
		});
		buttonPanel.add(spinner, "26, 4");

		JLabel lblKbps = new JLabel("kbps");
		buttonPanel.add(lblKbps, "28, 4");

		JPanel TabQueue = new JPanel();
		TabbedPane.addTab(LANG.getString("frmMain.Tabs.Queue"), null, TabQueue, null);
		TabQueue.setLayout(new BorderLayout(0, 0));

		TabQueue.add(buttonPanel, BorderLayout.SOUTH);

		TabQueue.add(TabQueues, BorderLayout.CENTER);
		
		cmbCategory.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent arg0) {
				changeCategory();
			}
		});
		
		QueuePanel.revalidate();
		ss1.expand();
	}
	
	public void initMenuBar(){
		JMenuBar mnuBar = new JMenuBar();
		JMenu mnuFile = new JMenu();
		JMenuItem mnuQuit = new JMenuItem();
		mnuAcc = new JMenu();
		
		mnuFile.setText(LANG.getString("frmMain.menu.File"));

		mnuQuit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_MASK));
		mnuQuit.setText(LANG.getString("frmMain.menu.Quit"));
		mnuQuit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				mnuQuitActionPerformed();
			}
		});
		mnuFile.add(mnuQuit);

		mnuBar.add(mnuFile);

		mnuAcc.setText(LANG.getString("frmMain.menu.Account"));
		mnuBar.add(mnuAcc);
				
		JSeparator separator = new JSeparator();
		JMenuItem mntmAddAccount = new JMenuItem(LANG.getString("frmMain.menu.AddAccount"));
		mntmAddAccount.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				mntmAddAccountActionPerformed();
			}
		});
		mnuAcc.add(mntmAddAccount);
		mnuAcc.add(separator);

		JMenu menu = new JMenu("?");
		mnuBar.add(menu);

		JMenuItem mntmDonate = new JMenuItem(LANG.getString("frmMain.menu.Donate"));
		menu.add(mntmDonate);
		mntmDonate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				donateButton();
			}
		});

		JMenuItem mntmAbout = new JMenuItem(LANG.getString("frmMain.menu.About"));
		mntmAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				FrmAbout about = new FrmAbout();
				about.setVisible(true);
			}
		});
		menu.add(mntmAbout);
		
		JMenuItem mntmShowLogfile = new JMenuItem(LANG.getString("frmMain.menu.ShowLogfile"));
		mntmShowLogfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				DesktopUtil.openDir(new File(System.getProperty("user.home")+"/YouPloader"));
			}
		});
		menu.add(mntmShowLogfile);
		
		chckbxmntmCheckForUpdates = new JCheckBoxMenuItem(LANG.getString("frmMain.menu.CheckforUpdates"));
		menu.add(chckbxmntmCheckForUpdates);
		if(s.setting.get("notify_updates").equals("1")){
			chckbxmntmCheckForUpdates.setSelected(true);
		}
		chckbxmntmCheckForUpdates.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				toggleUpdateNotifier();
			}
		});
		setJMenuBar(mnuBar);
	}

	protected void toggleUpdateNotifier() {
		if(chckbxmntmCheckForUpdates.isSelected()){
			s.setting.put("notify_updates", "1");
		}else{
			s.setting.put("notify_updates", "0");
		}
		s.save("notify_updates");
	}

	protected void donateButton() {
		DesktopUtil.openBrowser("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=AZ42BHSUTGPT6");
	}

	protected void startUploads() {
		if ("0".equals(s.setting.get("tos_agreed")) && !this.tos) {
			JCheckBox checkbox = new JCheckBox(LANG.getString("frmMain.tos.Remember"));
			String message = LANG.getString("frmMain.tos.Message");
			Object[] params = { message, checkbox };
			int n;
			do {
				n = JOptionPane.showConfirmDialog(null, params, LANG.getString("frmMain.tos.Title"),
						JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
			} while (n == JOptionPane.CLOSED_OPTION);
			if (n == JOptionPane.OK_OPTION) {
				if (checkbox.isSelected()) {
					s.setting.put("tos_agreed", "1");
					s.save("tos_agreed");
				}
				this.tos = true;
				UploadMgr.start();
			}
		} else {
			UploadMgr.start();
		}
	}

	private void mnuQuitActionPerformed() {
		System.exit(0);
	}

	private void mntmAddAccountActionPerformed() {
		AddAccount acc = new AddAccount(this);
		acc.setVisible(true);

	}

	public void prepModal(Account Account, String code) {
		modal = new ModalDialog((Frame) this, Account, code);
	}

	public void showModal() {
		modal.setVisible(true);
	}

	public void closeModal() {
		modal.success();
	}

	public void refreshAccounts() {
		int s = _accounts.size();
		for (int i = 0; i < s; i++) {
			mnuAcc.remove(_accounts.get(i));
			_accounts.remove(i);
		}
		cmbAccount.removeAllItems();
		btnAddToQueue.setEnabled(false);
		loadAccounts();
	}

	public void loadAccounts() {
		int i = 0;
		HashMap<AccountType, Integer> accounts = accMng.load();
		for (Entry<AccountType, Integer> entry : accounts.entrySet()) {
			cmbAccount.addItem(entry.getKey());
			JMenuItem rdoBtn = new JMenuItem(entry.getKey().toString());
			if (entry.getValue() == 1) {
				rdoBtn.setSelected(true);
				cmbAccount.setSelectedItem(entry.getKey());
			}
			rdoBtn.setActionCommand(entry.getKey().toString());
			rdoBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					editAccount(e.getActionCommand());
				}
			});
			_accounts.put(i, rdoBtn);
			mnuAcc.add(rdoBtn);
			i++;
		}
		if (!accounts.isEmpty()) {
			btnAddToQueue.setEnabled(true);
		}

	}

	protected void editAccount(String name) {
		int id = accMng.getId(name);
		EditAccount accedit = new EditAccount(this, name, id);
		accedit.setVisible(true);
	}
	
	private void loadQueue() throws SQLException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		PreparedStatement prest = null;
		Connection c = SQLite.getInstance();
		String sql = "SELECT * FROM `uploads` ORDER BY `id`";
		prest = c.prepareStatement(sql);
		ResultSet rs = prest.executeQuery();
		if (rs.isBeforeFirst()) {
			while (rs.next()) {
				UploadItem f = new UploadItem();
				Video v = mapper.readValue(rs.getString("data"), new TypeReference<Video>() {});
				VideoMetadata metadata;
				try{
					metadata = mapper.readValue(rs.getString("metadata"), new TypeReference<VideoMetadata>() {});
				}catch(NullPointerException e){
					metadata = new VideoMetadata();
				}
				f.upload_id = rs.getInt("id");
				String url = rs.getString("url");
				String yt_id = rs.getString("yt_id");
				f.getlblUrl().setText("https://www.youtube.com/watch?v=" + yt_id);
				f.getlblName().setText(v.snippet.title);
				metadata.setFrame(f);
				File data = new File(rs.getString("file"));
				String status = rs.getString("status");
				long position = rs.getLong("uploaded");
				long size = rs.getLong("lenght");
				if (url != null && !url.equals("") && !"FINISHED".equals(status)) {
					UploadMgr.addResumeableUpload(data, v, metadata, url, yt_id);
					f.getProgressBar().setString(String.format("%6.2f%%", (float) position / size * 100));
					f.getProgressBar().setValue((int) ((float) position / size * 100));
					f.getProgressBar().revalidate();
					f.revalidate();
					f.repaint();
				} else if ("NOT_STARTED".equals(status)) {
					UploadMgr.addUpload(data, v, metadata);
				} else if ("FAILED".equals(status)) {
					f.getBtnEdit().setEnabled(false);
					f.getProgressBar().setValue(0);
					f.getProgressBar().setString("Failed");
				} else {
					f.getBtnEdit().setEnabled(false);
					f.getProgressBar().setValue(100);
					f.getProgressBar().setString("Finished");
				}
				this.getQueuePanel().add(f, new CC().wrap());
				this.getQueuePanel().revalidate();

			}
			rs.close();
			prest.close();
		}
	}

	/**
	 * Creates the Upload and the Uploadframe
	 * 
	 * 
	 * @param File path to the file that should be uploaded
	 * 
	 * @param Name The title of the upload
	 * 
	 * @param acc_id The Account id of the Account that should upload the Video
	 * 
	 * @return UploadItem The Upload Frame
	 *  
	 */
	public UploadItem createUpload(String File, String Name, int acc_id) {
		UploadItem f = new UploadItem();
		EditPanel edit = (EditPanel) ss1.contentPane;
		f.getlblName().setText(Name);
		this.getQueuePanel().add(f, new CC().wrap());
		this.getQueuePanel().revalidate();
		Video v = new Video();
		v.snippet.title = Name;
		Categories cat = (Categories) cmbCategory.getSelectedItem();
		v.snippet.categoryId = cat.getID();
		v.snippet.description = txtDescription.getText();
		if (txtTags != null && !txtTags.getText().equals("")) {
			v.snippet.tags = trimTags(txtTags.getText());
		}
		VideoMetadata metadata = createMetadata();
		VisibilityType visibility = (VisibilityType) edit.getCmbVisibility().getSelectedItem();
		if (visibility == VisibilityType.SCHEDULED) {
			if (edit.getDateTimePicker().getEditor().getValue() != null
					&& !edit.getDateTimePicker().getEditor().getValue().equals("")) {
				v.status.privacyStatus = VisibilityType.SCHEDULED.getData();
				Date date = edit.getDateTimePicker().getDate();
				String pattern = "yyyy-MM-dd'T'HH:mm:ss.sssZ";
				SimpleDateFormat formatter = new SimpleDateFormat(pattern);
				f.getlblRelease().setText(edit.getDateTimePicker().getEditor().getValue().toString());
				v.status.publishAt = formatter.format(date);
			} else {
				v.status.privacyStatus = VisibilityType.PRIVATE.getData();
			}
		} else {
			v.status.privacyStatus = visibility.getData();
			if (visibility == VisibilityType.PUBLIC) {
				f.getlblRelease().setText("public");
			}
		}
		metadata.setAccount(acc_id);
		v.status.embeddable = edit.getChckbxAllowEmbedding().isSelected();
		v.status.publicStatsViewable = edit.getChckbxMakeStatisticsPublic().isSelected();
		LicenseType license = (LicenseType) edit.getCmbLicense().getSelectedItem();
		v.status.license = license.getData();
		File data = new File(File);
		metadata.setFrame(f);
		UploadMgr.addUpload(data, v, metadata);
		return f;
	}

	/**
	 * Updates an existing upload 
	 * 
	 * @param File File path to the file that should be uploaded
	 * @param acc_id The Account id of the Account that should upload the Video
	 * @param upload_id The id of the Upload that should be updated
	 * @throws SQLException
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public void update(String File, int acc_id, int upload_id) throws SQLException, IOException {
		EditPanel edit = (EditPanel) ss1.contentPane;
		String sql = "SELECT `yt_id` FROM `uploads` WHERE `id`=" + upload_id;
		PreparedStatement prest = null;
		Connection c = SQLite.getInstance();
		prest = c.prepareStatement(sql);
		ResultSet rs = prest.executeQuery();
		if (rs.isBeforeFirst()) {
			while (rs.next()) {
				String video_id = rs.getString("yt_id");
				Video v = new Video();
				v.snippet.title = txtTitle.getText();
				Categories cat = (Categories) cmbCategory.getSelectedItem();
				v.snippet.categoryId = cat.getID();
				v.snippet.description = txtDescription.getText();
				if (txtTags != null && !txtTags.getText().equals("")) {
					v.snippet.tags = trimTags(txtTags.getText());
				}
				VideoMetadata metadata = createMetadata();
				VisibilityType visibility = (VisibilityType) edit.getCmbVisibility().getSelectedItem();
				if (visibility == VisibilityType.SCHEDULED) {
					if (edit.getDateTimePicker().getEditor().getValue() != null
							&& !edit.getDateTimePicker().getEditor().getValue().equals("")) {
						v.status.privacyStatus = VisibilityType.SCHEDULED.getData();
						Date date = edit.getDateTimePicker().getDate();
						String pattern = "yyyy-MM-dd'T'HH:mm:ss.sssZ";
						SimpleDateFormat formatter = new SimpleDateFormat(pattern);
						v.status.publishAt = formatter.format(date);
					} else {
						v.status.privacyStatus = VisibilityType.PRIVATE.getData();
					}
				} else {
					v.status.privacyStatus = visibility.getData();
				}
				LicenseType license = (LicenseType) edit.getCmbLicense().getSelectedItem();
				String enddir = edit.getTxtEndDir().getText();
				v.status.embeddable = edit.getChckbxAllowEmbedding().isSelected();
				v.status.publicStatsViewable = edit.getChckbxMakeStatisticsPublic().isSelected();
				v.status.license = license.getData();
				if (video_id != null && !video_id.equals("")) {
					SQLite.updateUploadData(v,metadata, upload_id);
				} else {
					File data = new File(File);
					SQLite.updateUpload(acc_id, data, v, enddir, metadata, upload_id);
				}
				File data = new File(File);
				UploadMgr.updateUpload(upload_id, data, v, metadata, acc_id);
			}
		}
	}

	protected void resetEdit() {
		if (this.editItem != -1) {
			btnAddToQueue.setText(LANG.getString("frmMain.addtoQueue"));
			cmbFile.removeAllItems();
			this.editItem = -1;
		}
	}
	
	private void queueButton() {
		AccountType acc = (AccountType) cmbAccount.getSelectedItem();
		if (this.editItem != -1) {
			try {
				update(cmbFile.getSelectedItem().toString(), acc.getValue(), this.editItem);
			} catch (SQLException | IOException e) {
				LOG.error("Error updating upload",e);
			}
			btnAddToQueue.setText(LANG.getString("frmMain.addtoQueue"));
			cmbFile.removeAllItems();
			this.editItem = -1;
		} else {
			if (cmbFile.getSelectedItem() != null && !cmbFile.getSelectedItem().toString().equals("")) {
				createUpload(cmbFile.getSelectedItem().toString(), txtTitle.getText(), acc.getValue());
				cmbFile.removeAllItems();
			} else {
				JOptionPane.showMessageDialog(null, "You have to select a file.", "Give me something to work with!",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public void removeItem(int upload_id) {
		for (int i = 0; i < this.getQueuePanel().getComponentCount(); i++) {
			UploadItem item = (UploadItem) this.getQueuePanel().getComponent(i);
			if (item.upload_id == upload_id) {
				this.getQueuePanel().remove(i);
				this.getQueuePanel().revalidate();
				this.getQueuePanel().repaint();
			}
		}
	}

	public void editUpload(int upload_id) throws SQLException, IOException {
		this.editItem = upload_id;
		Connection c = SQLite.getInstance();
		btnAddToQueue.setText(LANG.getString("frmMain.updateUpload"));
		TabbedPane.setSelectedIndex(0);
		ObjectMapper mapper = new ObjectMapper();
		PreparedStatement prest = null;
		EditPanel edit = (EditPanel) ss1.contentPane;
		String sql = "SELECT * FROM `uploads` WHERE `id`=" + upload_id;
		prest = c.prepareStatement(sql);
		ResultSet rs = prest.executeQuery();
		if (rs.isBeforeFirst()) {
			while (rs.next()) {
				Video v = mapper.readValue(rs.getString("data"), new TypeReference<Video>() {
				});
				VideoMetadata metadata = mapper.readValue(rs.getString("metadata"), new TypeReference<VideoMetadata>() {
				});
				resetMetadata(metadata);
				cmbFile.removeAllItems();
				cmbFile.addItem(rs.getString("file"));
				this.setCategory(v.snippet.categoryId);
				txtTitle.setText(v.snippet.title);
				cmbAccount.setSelectedItem(rs.getString("account"));
				txtDescription.setText(v.snippet.description);
				txtTags.setText(prepareTagsfromArray(v.snippet.tags));
				edit.setLicence(v.status.license);
				edit.setVisibility(v.status.privacyStatus, v.status.publishAt);
				edit.getChckbxAllowEmbedding().setSelected(v.status.embeddable);
				edit.getChckbxMakeStatisticsPublic().setSelected(v.status.publicStatsViewable);
				edit.getTxtEndDir().setText(metadata.getEndDirectory());
				if (v.status.publishAt != null && !v.status.publishAt.equals("")) {
					edit.getDateTimePicker().setEnabled(true);
					edit.getDateTimePicker().getEditor().setEnabled(true);
					String pattern = "yyyy-MM-dd'T'HH:mm:ss.sssZ";
					SimpleDateFormat formatter = new SimpleDateFormat(pattern);
					Date date;
					try {
						date = formatter.parse(v.status.publishAt);
						edit.getDateTimePicker().setDate(date);
					} catch (ParseException e) {
						LOG.error("Error: ", e);
					}
				}
				edit.revalidate();
				edit.repaint();

			}
			rs.close();
			prest.close();
		} else {
			rs.close();
			prest.close();
		}

	}

	public void createTemplate(String name) {
		EditPanel edit = (EditPanel) ss1.contentPane;
		Template t = new Template(name);
		Video v = new Video();
		VideoMetadata metadata = createMetadata();
		v.snippet.title = txtTitle.getText();
		Categories cat = (Categories) cmbCategory.getSelectedItem();
		v.snippet.categoryId = cat.getID();
		v.snippet.description = txtDescription.getText();
		if (txtTags != null && !txtTags.getText().equals("")) {
			v.snippet.tags = trimTags(txtTags.getText());
		}
		VisibilityType visibility = (VisibilityType) edit.getCmbVisibility().getSelectedItem();
		v.status.privacyStatus = visibility.getData();
		LicenseType license = (LicenseType) edit.getCmbLicense().getSelectedItem();
		v.status.license = license.getData();
		v.status.embeddable = edit.getChckbxAllowEmbedding().isSelected();
		v.status.publicStatsViewable = edit.getChckbxMakeStatisticsPublic().isSelected();
		if (edit.getTxtStartDir() != null) {
			t.setStartdir(edit.getTxtStartDir().getText());
		}
		t.setMetadata(metadata);
		t.setVideodata(v);
		TemplateMgr.save(t);
		edit.refreshTemplates(t.name);
	}

	private String prepareTagsfromArray(String[] in) {
		String tags = "";
		for (int i = 0; i < in.length; i++) {
			if (i == 0) {
				tags = in[i];
			} else {
				tags += "," + in[i];
			}
		}
		return tags;
	}
	
	private String[] trimTags(String in){
		String[] tags = in.split(",");
		String[] trimmedtags = new String[tags.length];
		for (int i = 0; i < tags.length; i++) {
			trimmedtags[i] = tags[i].trim();
		}
		return trimmedtags;
	}

	public void loadTemplate(Item item) {
		EditPanel edit = (EditPanel) ss1.contentPane;
		Template t = item.getTemplate();
		this.setCategory(t.videodata.snippet.categoryId);
		txtTitle.setText(t.videodata.snippet.title);
		txtDescription.setText(t.videodata.snippet.description);
		txtTags.setText(prepareTagsfromArray(t.videodata.snippet.tags));
		edit.setLicence(t.videodata.status.license);
		if (t.videodata.status.publishAt != null && t.videodata.status.publishAt.equals("1")
				&& t.videodata.status.privacyStatus.equals("private")) {
			edit.getCmbVisibility().setSelectedItem(VisibilityType.SCHEDULED);
		} else if (t.videodata.status.publishAt != null && t.videodata.status.publishAt.equals("0")
				&& t.videodata.status.privacyStatus.equals("private")) {
			edit.getCmbVisibility().setSelectedItem(VisibilityType.PRIVATE);
		} else {
			edit.setVisibility(t.videodata.status.privacyStatus, "");
		}
		edit.getChckbxAllowEmbedding().setSelected(t.videodata.status.embeddable);
		edit.getChckbxMakeStatisticsPublic().setSelected(t.videodata.status.publicStatsViewable);
		if (t.startdir != null) {
			edit.getTxtStartDir().setText(t.startdir);
		}
		if(t.getMetadata()!=null){
			resetMetadata(t.getMetadata());
		}
		calcNotifies();
	}

	private void calcNotifies() {
		if (txtTags.getText().length() > 450) {
			lblTagslenght.setForeground(Color.RED);
		} else {
			lblTagslenght.setForeground(Color.BLACK);
		}
		if (txtTags.getText().length() >= 501) {
			txtTags.setText(txtTags.getText().substring(0, 500));

		}
		lblTagslenght.setText("(" + txtTags.getText().length() + "/500)");

		if (txtDescription.getText().length() > 4900) {
			lblDesclenght.setForeground(Color.RED);
		} else {
			lblDesclenght.setForeground(Color.BLACK);
		}
		if (txtDescription.getText().length() >= 5001) {
			txtDescription.setText(txtDescription.getText().substring(0, 5000));

		}
		lblDesclenght.setText("(" + txtDescription.getText().length() + "/5000)");

		if (txtTitle.getText().length() > 90) {
			lbltitlelenght.setForeground(Color.RED);
		} else {
			lbltitlelenght.setForeground(Color.BLACK);
		}
		if (txtTitle.getText().length() >= 101) {
			txtTitle.setText(txtTitle.getText().substring(0, 100));

		}
		lbltitlelenght.setText("(" + txtTitle.getText().length() + "/100)");
	}

	public void saveTemplate(int id) {
		EditPanel edit = (EditPanel) ss1.contentPane;
		Video v = new Video();
		VideoMetadata metadata = createMetadata();
		Template t = TemplateMgr.get(id);
		t.setMetadata(metadata);
		v.snippet.title = txtTitle.getText();
		Categories cat = (Categories) cmbCategory.getSelectedItem();
		v.snippet.categoryId = cat.getID();
		v.snippet.description = txtDescription.getText();
		if (txtTags != null && !txtTags.getText().equals("")) {
			v.snippet.tags = trimTags(txtTags.getText());
		}
		VisibilityType visibility = (VisibilityType) edit.getCmbVisibility().getSelectedItem();
		if (visibility == VisibilityType.SCHEDULED) {
			v.status.publishAt = "1";
		} else {
			v.status.publishAt = "0";
		}
		v.status.privacyStatus = visibility.getData();
		LicenseType license = (LicenseType) edit.getCmbLicense().getSelectedItem();
		v.status.license = license.getData();
		v.status.embeddable = edit.getChckbxAllowEmbedding().isSelected();
		v.status.publicStatsViewable = edit.getChckbxMakeStatisticsPublic().isSelected();
		if (edit.getTxtStartDir() != null) {
			t.setStartdir(edit.getTxtStartDir().getText());
		}
		t.setVideodata(v);
		TemplateMgr.update(id, t);
		edit.refreshTemplates(t.name);
	}

	public void setCategory(int catId) {
		for (int i = 0; i < cmbCategory.getItemCount(); i++) {
			if (cmbCategory.getItemAt(i).getID() == catId) {
				cmbCategory.setSelectedIndex(i);
			}
		}
	}
	
	protected void changeCategory() {
		EditPanel edit = (EditPanel) ss1.contentPane;
		edit.getTxtGameTitle().setEnabled(false);
		if (cmbCategory.getSelectedItem() == Categories.GAMES) {
			edit.getTxtGameTitle().setEnabled(false); //Disabled until I can figure out how Youtube does it.
		}
	}
	
	protected void monetisation(boolean disable) {
		MonetPanel monet = (MonetPanel) ss3.contentPane;
		if (disable) {
			monet.getChckbxMonetize().setSelected(false);
			monet.getChckbxMonetize().setEnabled(false);
			monet.getChckbxOverlayads().setSelected(false);
			monet.getChckbxOverlayads().setEnabled(false);
			monet.getChckbxSkippableVideoads().setSelected(false);
			monet.getChckbxSkippableVideoads().setEnabled(false);
			monet.getChckbxSponsoredCards().setSelected(false);
			monet.getChckbxSponsoredCards().setEnabled(false);
			for (int i = 0; i < monet.getCmbContentSyndication().getItemCount(); i++) {
				if (monet.getCmbContentSyndication().getItemAt(i).getData().equals("everywhere")) {
					monet.getCmbContentSyndication().setSelectedIndex(i);
				}
			}
			monet.getCmbContentSyndication().setEnabled(false);
		}else{
			monet.getChckbxMonetize().setEnabled(true);
			monet.getCmbContentSyndication().setEnabled(true);
		}
	}
	
	public VideoMetadata createMetadata() {
		VideoMetadata meta = new VideoMetadata();
		EditPanel edit = (EditPanel) ss1.contentPane;
		MonetPanel monet = (MonetPanel) ss3.contentPane;
		meta.setMonetized(monet.getChckbxMonetize().isSelected());
		SyndicationType syn = (SyndicationType) monet.getCmbContentSyndication().getSelectedItem();
		meta.setSyndication(syn.getData());
		meta.setInstream(monet.getChckbxSkippableVideoads().isSelected());
		meta.setOverlay(monet.getChckbxOverlayads().isSelected());
		meta.setProduct(monet.getChckbxSponsoredCards().isSelected());
		meta.setThumbnail(edit.getTxtThumbnail().getText().trim());
		meta.setProductplacement(monet.getChckbxProductplacement().isSelected());
		if(edit.getCmbVisibility().getSelectedItem() == VisibilityType.PUBLIC || edit.getCmbVisibility().getSelectedItem() == VisibilityType.SCHEDULED){
			meta.setMessage(edit.getTxtMessage().getText());
		}
		meta.setShare_fb(edit.getChckbxFacebook().isSelected());
		meta.setShare_gplus(edit.getChckbxGoogle().isSelected());
		meta.setShare_twitter(edit.getChckbxTwitter().isSelected());
		meta.setCommentsEnabled(edit.getChckbxAllowComments().isSelected());
		meta.setRestricted(edit.getChckbxAgeRestriction().isSelected());
		meta.setEndDirectory(edit.getTxtEndDir().getText());
		return meta;
	}
	
	private void resetMetadata(VideoMetadata metadata) {
		EditPanel edit = (EditPanel) ss1.contentPane;
		MonetPanel monet = (MonetPanel) ss3.contentPane;		
		for (int i = 0; i < monet.getCmbContentSyndication().getItemCount(); i++) {
			if (monet.getCmbContentSyndication().getItemAt(i).getData().equals(metadata.getSyndication())) {
				monet.getCmbContentSyndication().setSelectedIndex(i);
			}
		}
		edit.getTxtThumbnail().setText(metadata.getThumbnail());
		monet.getChckbxMonetize().setSelected(metadata.isMonetized());
		monet.getChckbxSkippableVideoads().setSelected(metadata.isInstream());
		monet.getChckbxOverlayads().setSelected(metadata.isOverlay());
		monet.getChckbxSponsoredCards().setSelected(metadata.isProduct());
		monet.getChckbxProductplacement().setSelected(metadata.getProductplacement());
		edit.getTxtMessage().setText(metadata.getMessage());
		edit.getChckbxFacebook().setSelected(metadata.isShare_fb());
		edit.getChckbxGoogle().setSelected(metadata.isShare_gplus());
		edit.getChckbxTwitter().setSelected(metadata.isShare_twitter());
		edit.getChckbxAllowComments().setSelected(metadata.isCommentsEnabled());
		edit.getChckbxAgeRestriction().setSelected(metadata.isRestricted());
		if (metadata.getEndDirectory() != null) {
			edit.getTxtEndDir().setText(metadata.getEndDirectory());
		}
	}

	public void deleteTemplate(int id) {
		EditPanel edit = (EditPanel) ss1.contentPane;
		TemplateMgr.delete(id);
		edit.refreshTemplates();
	}

	private JPanel getQueuePanel() {
		return QueuePanel;
	}

	public JTextArea getTxtTags() {
		return txtTags;
	}

	public JTextArea getTxtDescription() {
		return txtDescription;
	}

	public JSpinner getSpinner() {
		return spinner;
	}

	public static int getDBVersion() {
		return DB_VERSION;
	}
}
