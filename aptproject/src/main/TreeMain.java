package main;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import Edit.InvEditPan;
import Edit.RetunPan;
import apt.test.NewAptPanel;
import aptuser.ModifyAdmin;
import aptuser.ModifyUser;
import aptuser.RegistUser;
import chat.ChatClient;
import chat.ChatServer;
import complex.regist.ComplexPanel;
import db.AptuserModel;
import db.DBManager;
import dto.Aptuser;
import dto.MenuDto;
import message.MessageAutoInsertThread;
import message.RecieveMessage;
import message.SendMessage;
import message.SendMessageList;
import statistics.ChartMain;
import viewer.Admin_InvoiceView;
import viewer.Admin_UserView;
import viewer.User;

public class TreeMain extends JFrame implements TreeSelectionListener, ActionListener {

	private DBManager instance;
	private Connection con;

	int winWidth = 900;
	int winHeight = 700;
	int westWidth = 200;
	int centerWidth = 700;
	int centerHeight = 700;
	int westNorthHeight=150;
	int westSouthHeight=150;
	int treeScrollHeight = winHeight-westNorthHeight-westSouthHeight;

	private AptuserModel aptuser;
	private String userID;
	private String userName;
	private String adminUserID;
	private boolean adminFlag = false;
	private boolean adminMenuFlag = false;
	private String serverIP;
	private ChatServer chatSrv;
	private ChatClient chatClient;
	private RecieveMessage recieveMessage;
	private SendMessageList sendMessageList;
	private MessageAutoInsertThread msgAutoInsertThread;

	JTree tree;
	JScrollPane scroll;
	JPanel p_west, p_center, p_west_center, p_west_south, p_west_north;
	JLabel la_welcom;
	JButton bt_exit;
	BufferedImage image=null;
	Canvas  canLogo;

	Object currObject = new Object();

	DefaultMutableTreeNode root;
	Vector<Object> menuOpenList = new Vector<Object>();
	Vector<MenuDto> menuDtoList = new Vector<MenuDto>();
	Vector<JPanel> panelList = new Vector<JPanel>();
	ArrayList<Aptuser> userList;

	public TreeMain(DBManager instance, String userID) {

		this.instance = instance;
		this.con = instance.getConnection();
		this.userID = userID;

		p_west = new JPanel();
		p_west_north = new JPanel();
		p_west_center = new JPanel();
		p_west_south = new JPanel();
		p_center = new JPanel();

		root = new DefaultMutableTreeNode("Menu");
		tree = new JTree(root);
		scroll = new JScrollPane(tree);

		la_welcom = new JLabel("");
		bt_exit = new JButton("????");
		
		// Logo Image
		try {
			// Image Url
			URL url = this.getClass().getResource("/Apms.jpg");
			image = ImageIO.read(url);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		// Logo Canvas
		canLogo = new Canvas(){
			public void paint(Graphics g) {
				g.drawImage(image, 0, 0, westWidth, westNorthHeight, this);
			}
		};

		// root menu menuDtoList ?? ????
		MenuDto menuDto = new MenuDto();
		menuDto.setMenu_id(0);
		menuDto.setMenu_name(root.getUserObject().toString());
		menuDtoList.add(menuDto);

		scroll.setBackground(Color.YELLOW);

		// Size
		scroll.setPreferredSize(new Dimension(westWidth, treeScrollHeight));
		p_west_center.setPreferredSize(new Dimension(westWidth, treeScrollHeight));
		// System.out.println(p_west_center.getHeight());
		canLogo.setPreferredSize(new Dimension(westWidth, westNorthHeight));
		p_west_north.setPreferredSize(new Dimension(westWidth, westNorthHeight));
		p_west_south.setPreferredSize(new Dimension(westWidth, westSouthHeight));
		la_welcom.setPreferredSize(new Dimension(westWidth - 10, 50));
		bt_exit.setPreferredSize(new Dimension(100, 30));
		la_welcom.setPreferredSize(new Dimension(westWidth - 10, 50));

		// la_welcom text Bold, ?????? ????
		la_welcom.setFont(new Font("Default", Font.BOLD, 15));
		la_welcom.setHorizontalAlignment(JLabel.CENTER);
		la_welcom.setForeground(Color.BLUE);

		p_west_center.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		p_west_center.setLayout(new BorderLayout());
		p_west_center.add(scroll);

		p_west_south.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		p_west_south.setBackground(Color.WHITE);
		p_west_south.add(la_welcom);
		p_west_south.add(bt_exit);
		
		p_west_north.setLayout(new BorderLayout());
		p_west_north.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		p_west_north.add(canLogo);

		p_west.setLayout(new BorderLayout());
		p_west.add(p_west_north, BorderLayout.NORTH);
		p_west.add(p_west_center);
		p_west.add(p_west_south, BorderLayout.SOUTH);

		p_center.setLayout(new BorderLayout());

		add(p_west, BorderLayout.WEST);
		add(p_center);

		// Style
		tree.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 5));
		bt_exit.setFont(new Font("Default", Font.BOLD, 13));
		bt_exit.setBackground(new Color(247, 146, 30));
		//bt_exit.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

		// ?????? ????.
		tree.addTreeSelectionListener(this);
		tree.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				//System.out.println("tree Click");
				openWindowOnTree();
			}
		});
		bt_exit.addActionListener(this);
		// ???????? ?????? ???? ?????? ??????
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				close();
			}
		});

		// ???? ????
		init();
	
		setTitle("***** ?????????? *****");
		setVisible(true);
		setSize(winWidth, winHeight);
		setResizable(false);
		setLocationRelativeTo(null);

	}

	public void init() {
		/* --------------------------- ADMIN INFO --------------------------- */
		// aptuser?????????? ???????? ???????? ?????? ????????
		aptuser = new AptuserModel(con, "admin");
		userList = aptuser.getData();
		//System.out.println("userList.size()="+userList.size());
		
		// admin ???? ???????? ????
		if (userList.size()==0){
			JOptionPane.showMessageDialog(this, "admin ?????? ???????? ????????.\n?????????? ??????????.");
			close();
		}

		// ?????? IP?? ?????? ???? (???? ?????????????? ?????? ?????? ?? ????)
		serverIP = ((Aptuser) aptuser.getData().get(0)).getAptuser_ip();
		//System.out.println("serverIP = " + serverIP);

		// adminUserID ???? ??????.
		adminUserID = ((Aptuser) aptuser.getData().get(0)).getAptuser_id();

		/* --------------------------- ???? USER --------------------------- */
		// UserName ????
		updateUser();

		// userID ?? admin ?? ???? adminFlag ????
		if (userID.equalsIgnoreCase("admin")) {
			adminFlag = true;
		}

		// ?????? ?????? ?????? ???????? Main?? ?????????? ????????
		// ?????? ?????? Aptuser_perm=9 ?? ????, admin Menu ???? ????.
		if (userList.get(0).getAptuser_perm() == 9) {
			adminMenuFlag = true;
		}
		// System.out.println("adminFlag="+adminFlag);

		/* -------------------------- Tree ???? ???? -------------------------- */
		makeTree();

		// (????, ???? ???????? Message Insert ???? Thread ????)
		msgAutoInsertThread = new MessageAutoInsertThread(this);
		msgAutoInsertThread.setThreadFlag(true);
	}

	/* --------------------------- GETTER ???? --------------------------- */
	// get Connection
	public Connection getConnection() {
		return con;
	}

	// get UserId
	public String getUserID() {
		return userID;
	}

	// get ServrIP
	public String getSeverIP() {
		return serverIP;
	}

	public boolean getAdminFlag() {
		return adminFlag;
	}

	public String getAdminUserID() {
		return adminUserID;
	}
	/* ---------------------------- GETTER ?? ---------------------------- */

	/* -------------------- ?????????? ???????? method -------------------- */
	// ???? ???? ?????? ???????? ???? ???????? ????
	public void updateUser() {
		aptuser.selectDataByID(userID);
		//System.out.println("???? userList.size()="+userList.size());
		if (userList.size()==0){
			JOptionPane.showMessageDialog(this, "??,???? ?????? ???????? ????????. \n ?????????? ??????????.");
			close();
		}
		userName = userList.get(0).getAptuser_name();
		la_welcom.setText(userName + " ?? ??????????");
		refreshUI();
	}
	
	// ?????? ?????? ????/???? ???? ?????? ?????? ?????? ???????? ????
	public void refreshUI() {
		this.revalidate();
		for (JPanel pan : panelList) {
			pan.updateUI();
		}
	}

	// ???????? ?????? ???? ??????
	public void close() {
		// ???? Server ????
		if (chatSrv != null) {
			chatSrv.close();
		}

		// ???? Client ????
		for (Object obj : menuOpenList) {
			if (obj == chatClient) {
				chatClient.getThread().disconnect();
			}
		}
		
		if (msgAutoInsertThread!=null){
			msgAutoInsertThread.setThreadFlag(false);
		}
		
		// ???? ?????? List Thread ????
		if (sendMessageList!=null){
			sendMessageList.setThreadFlag(false);
		}
		
		// ???? ?????? Thread ????
		if (recieveMessage!=null){
			recieveMessage.setThreadFlag(false);
		}
		
		// Connection ????
		instance.disConnect(con);

		// Window Close
		System.exit(0);
	}
	/* ------------------------------------------------------------------ */

	// Tree ???? ????
	public void makeTree() {
		// System.out.println("makeTree");

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		PreparedStatement pstmtSub = null;
		ResultSet rsSub = null;

		// ???? ????
		StringBuffer sql = new StringBuffer();
		sql.append(" select m.menu_id, m.menu_level, m.menu_up_level_id, m.menu_name \n");
		sql.append("         , m.menu_class_name, m.menu_type, m.order_seq \n");
		sql.append("         , nvl(m.admin_role_flag,'N') admin_role_flag, nvl(m.user_role_flag,'N') user_role_flag \n");
		sql.append("         , nvl(m.menu_use_flag,'Y') menu_use_flag \n");
		sql.append("         , (select count(*) from menulist s \n");
		sql.append("	           where  s.MENU_UP_LEVEL_ID = m.menu_id \n");
		sql.append("             and     nvl(s.menu_use_flag,'Y')='Y') subcnt \n");
		sql.append(" from   menulist m \n");
		sql.append(" where  m.menu_level = 1 \n");
		sql.append(" and      nvl(m.menu_use_flag,'Y') = 'Y' ");
		sql.append(" order by m.order_seq \n");
		// System.out.println("sql : "+sql.toString());

		// ???? ????
		StringBuffer sqlSub = new StringBuffer();
		sqlSub.append(" select m.menu_id, m.menu_level, m.menu_up_level_id, m.menu_name \n");
		sqlSub.append("         , m.menu_class_name, m.menu_type, m.order_seq \n");
		sqlSub.append("         , nvl(m.admin_role_flag,'N') admin_role_flag, nvl(m.user_role_flag,'N') user_role_flag \n");
		sqlSub.append("         , nvl(m.menu_use_flag,'Y') menu_use_flag \n");
		sqlSub.append(" from   menulist m \n");
		sqlSub.append(" where m.menu_up_level_id = ? \n");
		sqlSub.append(" order by m.order_seq \n");
		// System.out.println("sqlSub : "+sqlSub.toString());

		try {
			pstmt = con.prepareStatement(sql.toString());
			rs = pstmt.executeQuery();

			// Menu ????
			while (rs.next()) {
				String menu = rs.getString("menu_name");

				DefaultMutableTreeNode node = null;
				node = new DefaultMutableTreeNode(menu);

				// node ?? menuDtoList ?? ????
				MenuDto menuDto = new MenuDto();
				menuDto.setMenu_id(rs.getInt("menu_id"));
				menuDto.setMenu_level(rs.getInt("menu_level"));
				menuDto.setMenu_up_level_id(rs.getInt("menu_up_level_id"));
				menuDto.setMenu_name(rs.getString("menu_name"));
				menuDto.setMenu_class_name(rs.getString("menu_class_name"));
				menuDto.setMenu_type(rs.getString("menu_type"));
				menuDto.setOrder_seq(rs.getInt("order_seq"));
				menuDto.setAdmin_role_flag(rs.getString("admin_role_flag"));
				menuDto.setUser_role_flag(rs.getString("user_role_flag"));
				menuDto.setMenu_use_flag(rs.getString("menu_use_flag"));

				// system user ?? ????,
				// Admin ???????? admin ???? ?????? ????,
				// ???? admin ?????? ??????, ???? ?????? ???? ???? ?? ????. ???? ????
				if ((adminMenuFlag == true && rs.getString("admin_role_flag").equalsIgnoreCase("Y"))
						|| (adminMenuFlag == false && rs.getString("user_role_flag").equalsIgnoreCase("Y"))) {
					//System.out.println("menu=" + rs.getString("menu_name"));
					root.add(node);
					menuDtoList.add(menuDto);
				}

				// SubMenu ????
				if (rs.getInt("subcnt") != 0) {
					pstmtSub = con.prepareStatement(sqlSub.toString());
					// System.out.println("menu_id="+rs.getInt("menu_id"));
					pstmtSub.setInt(1, rs.getInt("menu_id"));
					rsSub = pstmtSub.executeQuery();
					// System.out.println("rsSub = "+rsSub);

					while (rsSub.next()) {
						// Menu node ????
						DefaultMutableTreeNode nodeSub = null;
						nodeSub = new DefaultMutableTreeNode(rsSub.getString("menu_name"));

						// node ?? menuDtoList ?? ????
						MenuDto menuSubDto = new MenuDto();
						menuSubDto.setMenu_id(rsSub.getInt("menu_id"));
						menuSubDto.setMenu_level(rsSub.getInt("menu_level"));
						menuSubDto.setMenu_up_level_id(rsSub.getInt("menu_up_level_id"));
						menuSubDto.setMenu_name(rsSub.getString("menu_name"));
						menuSubDto.setMenu_class_name(rsSub.getString("menu_class_name"));
						menuSubDto.setMenu_type(rsSub.getString("menu_type"));
						menuSubDto.setOrder_seq(rsSub.getInt("order_seq"));
						menuSubDto.setAdmin_role_flag(rsSub.getString("admin_role_flag"));
						menuSubDto.setUser_role_flag(rsSub.getString("user_role_flag"));
						menuSubDto.setMenu_use_flag(rsSub.getString("menu_use_flag"));

						// Admin ???????? admin ???? ?????? ????, ???? admin ?????? ??????, ???? ??????
						// ???? ???? ?? ????. ???? ????
						if ((adminMenuFlag == true && rsSub.getString("admin_role_flag").equalsIgnoreCase("Y"))
								|| (adminMenuFlag == false
										&& rsSub.getString("user_role_flag").equalsIgnoreCase("Y"))) {
							//System.out.println("submenu=" + rsSub.getString("menu_name"));
							node.add(nodeSub);
							menuDtoList.add(menuSubDto);
						}

						// System.out.println("getMenu_name="+menuSubDto.getMenu_name());
					}
				}

			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (rsSub != null)
				try {
					rsSub.close();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			if (pstmtSub != null)
				try {
					pstmtSub.close();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			if (pstmt != null)
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}

		// tree ???? ??????
		//System.out.println("tree.getRowCount()=" + tree.getRowCount());
		int r = 0;
		while (r < tree.getRowCount()) {
			tree.expandRow(r);
			r++;
		}

		// ???? ???? ???? ????
		String firstMenuClasssName = "";
		if (adminMenuFlag) { // ??????
			firstMenuClasssName = "Admin_InvoiceView"; // ??????????????
		} else {
			firstMenuClasssName = "User"; // ??????????????
		}

		// ???????? TreeIndex ????
		int defaulTreeIndex = -1;
		int chatServerIndex = -1;
		String chkClassName = "";
		for (int i = 0; i < menuDtoList.size(); i++) {
			chkClassName = menuDtoList.get(i).getMenu_class_name();
			if (chkClassName != null) {
				// ???? open ?????? index Check
				// System.out.println("chkClassName = "+chkClassName);
				if (chkClassName.equalsIgnoreCase(firstMenuClasssName)) {
					defaulTreeIndex = i;
					// System.out.println("defaultSelectRow =
					// "+defaulTreeIndex);
				}

				// admin ????, ChatServer ?? index Check
				if (adminFlag == true && chkClassName.equalsIgnoreCase("ChatServer")) {
					chatServerIndex = i;
					// System.out.println("chatServerIndex = "+chatServerIndex);
				}
			}
		}

		// ??????????(admin)?? ???? Chat Server ????
		if (chatServerIndex != -1) {
			tree.setSelectionInterval(chatServerIndex, chatServerIndex);
		}

		// ???? ???? ?????? ???? ????, ?? ???? ????
		if (defaulTreeIndex != -1) {
			// tree.clearSelection();
			tree.setSelectionInterval(defaulTreeIndex, defaulTreeIndex);
		}

	}

	// Tree ?????? ????
	public void valueChanged(TreeSelectionEvent e) {
		// System.out.println("tree valueChanged");
		Object obj = e.getSource();
		if (obj == tree) {
			openWindowOnTree();
		}
	}

	public void removeMenuOpenList(Object openObject) {
		this.menuOpenList.remove(openObject);
	}

	// Tree ???? ???? ?????? ???? ???? Open
	public void openWindowOnTree() {
		// System.out.println("openWindowOnTree");
		// Title ??????
		this.setTitle("");

		// ?????? ???? node check
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

		// ??????
		String menuName = node.getUserObject().toString();
		// System.out.println("node : "+menuName);

		// menuDtoList ???? ???? ?????? index ????
		int dtoIndex = -1;
		for (int i = 0; i < menuDtoList.size(); i++) {
			if (menuDtoList.get(i).getMenu_name().equals(menuName)) {
				dtoIndex = i;
				break;
			}
		}
		// dto ?? ???? ???? skip
		// System.out.println("dtoIndex="+dtoIndex + ", menuName = "+menuName);
		if (dtoIndex == -1)
			return;

		// menuDto ???? ???? ?????? className ????????
		String className = menuDtoList.get(dtoIndex).getMenu_class_name();
		// System.out.println("className = "+className);

		// className ?? ???? ????, ???? ?????? ???????? (???? ???? ??????) skip
		if (className == null) {
			//JOptionPane.showMessageDialog(this, menuName + " ?????? Class ?? ???????? ??????????.");
			System.out.println(menuName+" Class ??????");
			return;
		}

		// menuType ?? Frame ???? Panel ???? ???????? open
		String menuType = menuDtoList.get(dtoIndex).getMenu_type();

		// System.out.println("menuName="+menuName+", className="+className+",
		// menuType="+menuType);

		if (menuType.equalsIgnoreCase("F")) {
			// Frame Open
			frameOpen(className);
		} else {
			// Panel Open
			panelOpen(className, menuName);
		}

	}

	// className ???? ???????? ???? index ????
	public int findOpenClassIndex(String className) {
		int index = -1;
		for (int i = 0; i < menuOpenList.size(); i++) {
			// System.out.println(menuOpenList.get(i).getClass().getSimpleName());
			if (menuOpenList.get(i).getClass().getSimpleName().equals(className)) {
				index = i;
				break;
			}
		}
		// System.out.println(className + " index = "+index);
		return index;
	}

	// Frame menu open
	public void frameOpen(String className) {

		// Frame ?? ???? ????
		int openFrameX = this.getX() + westWidth + 20;
		int openFramY = this.getY() + 40;

		// System.out.println("frameOpen");
		// ???? ???? ???? menu ???? ????
		int index = findOpenClassIndex(className);

		if (index != -1) {
			// ???? open ???? ?????? ?? ?????? ????????.
			((JFrame) menuOpenList.get(index)).toFront();
		} else {
			// open ???? ??????, new ???? open ????, menuOpenList ?? ????????.
			currObject = null;
			if (className.equalsIgnoreCase("ChatClient")) {
				// ????
				// System.out.println("ChatClient -------------------");
				chatClient = new ChatClient(this);
				menuOpenList.add(chatClient);
				currObject = chatClient;

			} else if (className.equalsIgnoreCase("SendMessage")) {
				// ???? ??????
				SendMessage send = new SendMessage(this);
				menuOpenList.add(send);
				currObject = send;

			} else if (className.equalsIgnoreCase("SendMessageList")) {
				// ???? ??????
				SendMessageList sendMsgList = new SendMessageList(this);
				menuOpenList.add(sendMsgList);
				currObject = sendMsgList;

			} else if (className.equalsIgnoreCase("RecieveMessage")) {
				// ???? ??????
				recieveMessage = new RecieveMessage(this);
				menuOpenList.add(recieveMessage);
				currObject = recieveMessage;
			}

			// ?????? Frame ?? ?????? ???? treeFrame ?? ?????? ???? ????????.
			if (currObject != null) {
				((JFrame) currObject).setLocation(openFrameX, openFramY);
			}

		}

	}

	// Panel menu open
	public void panelOpen(String className, String menuName) {

		// ?????? Panel ???? Visible=false
		for (int i = 0; i < panelList.size(); i++) {
			panelList.get(i).setVisible(false);
			// System.out.println(i+" : visible false");
		}

		// p_center ??????
		setTitle("");
		p_center.updateUI();

		// System.out.println("panelOpen");
		JPanel curPanel = new JPanel();

		// ???? ???? ???? menu ???? ????
		int index = findOpenClassIndex(className);
		// System.out.println(menuName + ", "+className + ", open index =
		// "+index);

		// ???? ???? ???? ????, ???? Panel ?? new ????.
		if (index == -1) {

			if (className.equalsIgnoreCase("InvEditPan")) {
				// ????????
				InvEditPan invEditPan = new InvEditPan(con);
				curPanel = invEditPan;
			} else if (className.equalsIgnoreCase("Admin_InvoiceView")) {
				// ??????????????
				Admin_InvoiceView adminInvoice = new Admin_InvoiceView(con);
				// System.out.println("adminInvoice = "+adminInvoice);
				curPanel = adminInvoice;
			} else if (className.equalsIgnoreCase("RetunPan")) {
				// ????????
				RetunPan returnPan = new RetunPan(con, userList);
				curPanel = returnPan;
			} else if (className.equalsIgnoreCase("User")) {
				// ??????????????
				User userPan = new User(userList, con);
				curPanel = userPan;
			} else if (className.equalsIgnoreCase("RegistUser")) {
				// ????????
				RegistUser registUser = new RegistUser(con, userID);
				curPanel = registUser;
			} else if (className.equalsIgnoreCase("Admin_UserView")) {
				// ????????
				Admin_UserView adminUserView = new Admin_UserView(con);
				curPanel = adminUserView;
			} else if (className.equalsIgnoreCase("ModifyAdmin")) {
				// ??????????????
				ModifyAdmin modifyAdmin = new ModifyAdmin(con, this);
				curPanel = modifyAdmin;
			} else if (className.equalsIgnoreCase("ModifyUser")) {
				// ????????????
				ModifyUser modifyUser = new ModifyUser(con, this);
				curPanel = modifyUser;
			} else if (className.equalsIgnoreCase("ComplexPanel")) {
				// ?????? ????
				ComplexPanel complexPanel = new ComplexPanel(con);
				curPanel = complexPanel;
			} else if (className.equalsIgnoreCase("ChatServer")) {
				// ????(????)
				chatSrv = new ChatServer(this);
				curPanel = chatSrv;
			} else if (className.equalsIgnoreCase("ChartMain")) {
				// Chart
				ChartMain  chartMain = new ChartMain();
				curPanel = chartMain;
			} else if (className.equalsIgnoreCase("NewAptPanel")) {
				// ?????? ????
				NewAptPanel  newAptPanel = new NewAptPanel(con);
				curPanel = newAptPanel;
				
			} else {
				// System.out.println("menu ????.");
				curPanel = null;
			}

			if (curPanel != null) {
				p_center.add(curPanel);

				// ?????? Panel ?? menuOpenList ?? ????
				menuOpenList.add(curPanel);

				// ?????? Panel ?? panelList ?? ????
				panelList.add(curPanel);
			}

			// menuOpenList ?? ???? ??????????, ???? index ?? ????
	    	index = findOpenClassIndex(className);
	    }
	    
	    //System.out.println("get index = "+index);
	    //System.out.println("panelList.size() = "+panelList.size());
	    
	    // index ?? -1 ?? ???? ????, ?? Panel ?? ???????? ???? ???? Panel visible=true
	    if (index!=-1){
	    	for (int i=0; i<panelList.size(); i++){
	    		if (panelList.get(i)==menuOpenList.get(index)){
					// Title ????
	    			
					setTitle(menuName);

					// panel ?????? p_center ?? ???????? ??????
					//panelList.get(i).setPreferredSize(new Dimension(centerWidth, centerHeight));
	    			//panelList.get(i).setSize(centerWidth, centerHeight);

	    			// panel ??????
					panelList.get(i).setVisible(true);

	    		}
	    	}
	    } else {
	    	//JOptionPane.showMessageDialog(this, "?????? ???????? ????????.");
	    	//System.out.println(menuName+" Class ??????");
	    }

		p_center.updateUI();
	}

	public void actionPerformed(ActionEvent e) {
		Object obj = e.getSource();
		if (obj == bt_exit) {
			close();
		}
	}

}
