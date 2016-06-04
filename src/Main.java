import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.json.simple.JSONObject;

import api.HabiticaClient;
import gui.CustomBar;
import gui.JXTrayIcon;

public class Main extends JFrame {
	private HabiticaClient client;
	private JPanel settingsPane = new JPanel();
	private JPanel userInfoPane = new JPanel();
	private JPanel tasksAndHabitsPane = new JPanel();
	private JPanel habitsPane = new JPanel(), dailiesPane = new JPanel(), taskPane = new JPanel();
	private JTabbedPane mainPane = new JTabbedPane();
	private JSplitPane mainTasksPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, userInfoPane, tasksAndHabitsPane);
	private CustomBar experienceBar = new CustomBar(Color.YELLOW, "XP");
	private CustomBar healthBar = new CustomBar(Color.RED, "Health");


	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
		} catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		new Main();
	}

	public Main() {
		client = new HabiticaClient();
		client.getUserInfo();
		client.requestTasks();
		this.setTitle("Habitica");
		this.setSize(new Dimension(720, 640));
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setContentPane(mainPane);
		this.setVisible(true);
		initUI();
		initTasks();
		try {
			initSystray();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public void initSystray() throws IOException {
		if(!SystemTray.isSupported()) {
			System.out.println("SystemTray not supported.");
			return;
		}
		final JXTrayIcon trayIcon = new JXTrayIcon(createImage("habitica.png", "Habitica"));
		final SystemTray tray = SystemTray.getSystemTray();
		final JPopupMenu mainMenu = new JPopupMenu();

		JMenu dailies = new JMenu("Dailies");
		JMenu todos = new JMenu("Todos");
		JMenu habits = new JMenu("Habits");

		for(Object obj : client.getDailies()) {
			final JSONObject json = (JSONObject)obj;
			final JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem((String)json.get("text"));
			menuItem.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent event) {
					if(event.getStateChange() == ItemEvent.SELECTED) {
						client.upgradeTask((String)json.get("id"), "up");
						menuItem.setSelected(true);
					}
				}
			});
			dailies.add(menuItem);
		}

		for(Object obj : client.getTasks()) {
			final JSONObject json = (JSONObject)obj;
			final JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem((String)json.get("text"));
			menuItem.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent event) {
					if(event.getStateChange() == ItemEvent.SELECTED) {
						client.upgradeTask((String)json.get("id"), "up");
						menuItem.setSelected(true);
					}
				}
			});
			todos.add(menuItem);
		}

		for(Object obj : client.getHabits()) {
		 	final JSONObject json = (JSONObject)obj;
		 	final JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem((String)json.get("text"));
		 	menuItem.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent event) {
					if(event.getStateChange() == ItemEvent.SELECTED) {
						client.upgradeTask((String)json.get("id"), "up");
						menuItem.setSelected(true);
					}
				}
			});
			habits.add(menuItem);
		}

		mainMenu.add(habits);
		mainMenu.add(dailies);
		mainMenu.add(todos);
		trayIcon.setImageAutoSize(true);
		trayIcon.setJPopupMenu(mainMenu);

		try {
			tray.add(trayIcon);
		} catch (AWTException e) {
			e.printStackTrace();
		}

	}

	public void initUI() {

		//HealthBar and ExperienceBar initialisation
		experienceBar.setMaximum((int)client.getToNextLevel());
		experienceBar.setValue((int)client.getExperience());
		healthBar.setMaximum(50);
		healthBar.setValue((int)client.getHp());

		//setups the Tasks view
		mainTasksPane.setDividerLocation(80);
		mainPane.addTab("Tasks", mainTasksPane);
		mainPane.addTab("Settings", settingsPane);

		//User stats
		userInfoPane.setLayout(new BoxLayout(userInfoPane, BoxLayout.PAGE_AXIS));
		userInfoPane.add(Box.createRigidArea(new Dimension(0, 10)));
		userInfoPane.add(experienceBar);
		userInfoPane.add(Box.createRigidArea(new Dimension(0, 5)));
		userInfoPane.add(healthBar);

		//User habits, dailies and tasks
		tasksAndHabitsPane.setLayout(new BoxLayout(tasksAndHabitsPane, BoxLayout.LINE_AXIS));
		habitsPane.setMaximumSize(new Dimension((this.getWidth() - 10) / 3, 400));
		habitsPane.setBorder(BorderFactory.createTitledBorder("Habits"));
		habitsPane.setLayout(new BoxLayout(habitsPane, BoxLayout.PAGE_AXIS));
		tasksAndHabitsPane.add(habitsPane);
		dailiesPane.setMaximumSize(new Dimension((this.getWidth() - 10) / 3, 400));
		dailiesPane.setBorder(BorderFactory.createTitledBorder("Dailies"));
		dailiesPane.setLayout(new BoxLayout(dailiesPane, BoxLayout.PAGE_AXIS));
		tasksAndHabitsPane.add(dailiesPane);
		taskPane.setMaximumSize(new Dimension((this.getWidth() - 10) / 3, 400));
		taskPane.setBorder(BorderFactory.createTitledBorder("Tasks"));
		taskPane.setLayout(new BoxLayout(taskPane, BoxLayout.PAGE_AXIS));
		tasksAndHabitsPane.add(taskPane);
	}

	public void initTasks() {
		for(int i = 0; i < client.getTasks().size(); i++) {
			final JSONObject current = (JSONObject)client.getTasks().get(i);
			final JCheckBox checkBox = new JCheckBox((String)current.get("text"));
			taskPane.add(checkBox);
			checkBox.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent event) {
					if(event.getStateChange() == ItemEvent.SELECTED) {
						client.upgradeTask((String)current.get("id"), "up");
						taskPane.remove(checkBox);
					}
				}
			});

		}
		for(int i = 0; i < client.getDailies().size(); i++) {
			JSONObject current = (JSONObject)client.getDailies().get(i);
			JCheckBox checkBox = new JCheckBox((String)current.get("text"));
			dailiesPane.add(checkBox);
		}
		for(int i = 0; i < client.getHabits().size(); i++) {
			JSONObject current = (JSONObject)client.getHabits().get(i);
			JCheckBox checkBox = new JCheckBox((String)current.get("text"));
			habitsPane.add(checkBox);
		}
	}

	protected static Image createImage(String path, String description) {
        URL imageURL = Main.class.getResource(path);

        if (imageURL == null) {
            System.err.println("Resource not found: " + path);
            return null;
        } else {
            return (new ImageIcon(imageURL, description)).getImage();
        }
    }
}
