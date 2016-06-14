import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.json.simple.JSONObject;

import api.HabiticaClient;
import gui.CustomBar;
import gui.JXTrayIcon;
import sun.tools.jar.resources.jar;

public class Main extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	private HabiticaClient client;
	private JPanel settingsPane = new JPanel();
	private JPanel userInfoPane = new JPanel();
	private JPanel tasksAndHabitsPane = new JPanel();
	private JPanel habitsPane = new JPanel(), dailiesPane = new JPanel(), taskPane = new JPanel();
	private JTabbedPane mainPane = new JTabbedPane();
	private JSplitPane mainTasksPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, userInfoPane, tasksAndHabitsPane);
	private CustomBar experienceBar = new CustomBar(Color.YELLOW, "XP");
	private CustomBar healthBar = new CustomBar(Color.RED, "Health");
	private boolean activate = false;
	private Timer timer;
	private JMenu dailies = new JMenu("Dailies"), todos = new JMenu("Todos"), habits = new JMenu("Habits");
	
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
		if(activate) {
			this.setVisible(true);
		}
		initUI();

		initTasks();
		try {
			initSystray();
		} catch(IOException e) {
			e.printStackTrace();
		}
		timer = new Timer(60000, this);
	}

	public void initSystray() throws IOException {
		if(!SystemTray.isSupported()) {
			System.out.println("SystemTray not supported.");
			return;
		}
		final JXTrayIcon trayIcon = new JXTrayIcon(createImage("habitica.png", "Habitica"));
		final SystemTray tray = SystemTray.getSystemTray();
		final JPopupMenu mainMenu = new JPopupMenu();
		
		JMenuItem createTask = new JMenuItem("Create Task");
		
		createTask.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				initTaskCreationPopup();
			}
		});
		
		for(Object obj : client.getDailies()) {
			final JSONObject json = (JSONObject)obj;
			final JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem((String)json.get("text"));
			menuItem.addItemListener(new TodoChangeItemListener(json, menuItem));
			dailies.add(menuItem);
		}

		for(Object obj : client.getTodos()) {
			final JSONObject json = (JSONObject)obj;
			final JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem((String)json.get("text"));
			menuItem.addItemListener(new TodoChangeItemListener(json, menuItem));
			todos.add(menuItem);
		}

		for(Object obj : client.getHabits()) {
		 	final JSONObject json = (JSONObject)obj;
		 	final JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem((String)json.get("text"));
		 	menuItem.addItemListener(new TodoChangeItemListener(json, menuItem));
			habits.add(menuItem);
		}
		
		mainMenu.add(habits);
		mainMenu.add(dailies);
		mainMenu.add(todos);
		mainMenu.addSeparator();;
		mainMenu.add(createTask);
		trayIcon.setImageAutoSize(true);
		trayIcon.setJPopupMenu(mainMenu);

		try {
			tray.add(trayIcon);
		} catch (AWTException e) {
			e.printStackTrace();
		}

	}
	
	public void initTaskCreationPopup() {
		Object[] types = {"habit", "todo", "daily"};
		String type = (String)JOptionPane.showInputDialog(this, "Type of task : ", "Create a task : step 1", JOptionPane.PLAIN_MESSAGE, null, types, types[1]);
		
		String title = (String)JOptionPane.showInputDialog(this, "Title of task : ", "Create a task : step 2", JOptionPane.PLAIN_MESSAGE, null, null, "Eat bananas everyday");
		
		String notes = (String)JOptionPane.showInputDialog(this, "Notes : ", "Create a task : step 3", JOptionPane.PLAIN_MESSAGE, null, null, "Created by habitica desktop client");
		
		try {
			client.createTask(title, type, notes);
		} catch (FileNotFoundException e) {
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

		//User statistics
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
		for(int i = 0; i < client.getTodos().size(); i++) {
			final JSONObject current = (JSONObject)client.getTodos().get(i);
			final JCheckBox checkBox = new JCheckBox((String)current.get("text"));
			taskPane.add(checkBox);
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
	
	public void refresh() {
		client.requestTasks(); //Gets the tasks from the webserver
		initTasks(); //Update the tasks in the main UI
		System.out.println(dailies.getItemCount());
		todos.removeAll();
		for(Object obj : client.getTodos()) {
			final JSONObject json = (JSONObject)obj;
			final JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem((String)json.get("text"));
			menuItem.addItemListener(new TodoChangeItemListener(json, menuItem));
			todos.add(menuItem);
		}
		dailies.removeAll();
		for(Object obj : client.getDailies()) {
			final JSONObject json = (JSONObject)obj;
			final JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem((String)json.get("text"));
			menuItem.addItemListener(new TodoChangeItemListener(json, menuItem));
			dailies.add(menuItem);
		}
		habits.removeAll();
		for(Object obj : client.getHabits()) {
			final JSONObject json = (JSONObject)obj;
			final JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem((String)json.get("text"));
			menuItem.addItemListener(new TodoChangeItemListener(json, menuItem));
			habits.add(menuItem);
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
	
	class TodoChangeItemListener implements ItemListener {
		private JSONObject json;
		private JMenuItem changer;
		
		public TodoChangeItemListener(JSONObject json, JMenuItem changer) {
			this.json = json;
			this.changer = changer;
		}
		
		@Override
		public void itemStateChanged(ItemEvent event) {
			if(event.getStateChange() == ItemEvent.SELECTED) {
				client.upgradeTask((String)json.get("id"), "up");
				changer.setSelected(true);
			} 
			if(event.getStateChange() == ItemEvent.DESELECTED) {
				client.upgradeTask((String)json.get("id"), "down");
				changer.setSelected(false); 
			}
		}
		
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		refresh();
	}
	
}
