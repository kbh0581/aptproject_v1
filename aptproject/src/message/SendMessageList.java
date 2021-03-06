package message;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

import main.TreeMain;

public class SendMessageList extends JFrame implements ActionListener , Runnable{
	
	Connection con;	
	TreeMain treeMain;
	
	JPanel  p_south, p_center, p_north, p_center_center, p_center_south;
	JTextArea   area;
	JTable   table, tableList;
	JScrollPane  scroll, areaScroll, listScroll;
	JTextField  t_input, t_title;
	JLabel  la_title;
	JButton  bt_search;
	
	SendMsgListModel  model;
	String userId;
	
	RecieveUserListModel recvListModel;
	int msg_send_id; // ?۽? Message id
	int colIndexOfSendId;
	
	int frameWidth=600;
	int frameHeight=600;
	
	boolean threadFlag=false;
	Thread  thread;
	
	public SendMessageList(TreeMain treeMain) {
		this.treeMain = treeMain;
		this.con = treeMain.getConnection();
		this.userId = this.treeMain.getUserID();
		
		//System.out.println("SendMessageList : userId="+userId);
		
		p_north = new JPanel();
		p_center = new JPanel();
		p_center_center = new JPanel();
		p_center_south = new JPanel();
		p_south = new JPanel();
		
		t_input = new JTextField();
		bt_search = new JButton("?˻?");
		
		table = new JTable();
		scroll = new JScrollPane(table);
		
		tableList = new JTable();
		listScroll = new JScrollPane(tableList);
		
		la_title = new JLabel("????");
		t_title = new JTextField();
		area = new JTextArea();
		areaScroll = new JScrollPane(area);
		
		// color
		t_title.setBackground(Color.WHITE);
		area.setBackground(Color.WHITE);
		
		
		p_north.add(t_input);
		p_north.add(bt_search);
		
		p_center.setLayout(new BorderLayout());
		
		p_center_center.setLayout(new BorderLayout());
		p_center_center.add(scroll);
		p_center_south.setLayout(new FlowLayout());
		p_center_south.add(la_title);
		p_center_south.add(t_title);
		p_center_south.add(areaScroll);
		
		p_center.setLayout(new BorderLayout());
		p_center.add(p_center_center);
		p_center.add(p_center_south, BorderLayout.SOUTH);		
		
		p_south.setLayout(new BorderLayout());
		p_south.add(listScroll);
		
		add(p_north, BorderLayout.NORTH);
		add(p_center);
		add(p_south, BorderLayout.SOUTH);
		
		// ?????? ????
		// ?˻? ??ư
		bt_search.addActionListener(this);
		t_input.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				int key = e.getKeyCode();
				if (key==KeyEvent.VK_ENTER){
					search("I");		
					showMessage();
					showRecvList();
				}
			}
		});
		// ???콺 ?̺?Ʈ
		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				showMessage();
				showRecvList();
			}
		});
		// Key ?̺?Ʈ
		table.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				int key = e.getKeyCode();
				if (key==KeyEvent.VK_UP || key==KeyEvent.VK_DOWN){
					showMessage();
					showRecvList();
				}
			}
		});
		// 
		this.addWindowListener(new WindowAdapter() {			
			public void windowClosing(WindowEvent e) {
				close();
			}
		});
		
		// size
		t_input.setPreferredSize(new Dimension(frameWidth-200, 20));
		p_north.setPreferredSize(new Dimension(frameWidth, 45));
		p_center_south.setPreferredSize(new Dimension(frameWidth, 150));
		p_south.setPreferredSize(new Dimension(frameWidth, 180));
		t_title.setPreferredSize(new Dimension(frameWidth-160, 20));
		areaScroll.setPreferredSize(new Dimension(frameWidth-110, 110));
		table.setRowHeight(20);
		tableList.setRowHeight(20);
		
		// Editable
		t_title.setEditable(false);
		area.setEditable(false);
		
		// Color
		p_north.setBackground(new Color(247, 146, 30));
		p_center_south.setBackground(new Color(247, 146, 30));
		
		init();
		
		setTitle("???? ?۽???");
		setVisible(true);
		setSize(frameWidth, frameHeight);
		
	}
	
	public void init(){
		
		// ?۽? List
		model = new SendMsgListModel(con, userId, "");
		table.setModel(model);
		
		// ?÷? Ŭ???? ?ش? Column ???? ????
		table.setRowSorter(new TableRowSorter(model));
		
		// msg_send_content ?÷? ??????
		table.getColumn("msg_send_content").setWidth(0);
		table.getColumn("msg_send_content").setMinWidth(0);
		table.getColumn("msg_send_content").setMaxWidth(0);
	
		// msg_send_content ?÷? ??????
		table.getColumn("msg_send_id").setWidth(0);
		table.getColumn("msg_send_id").setMinWidth(0);
		table.getColumn("msg_send_id").setMaxWidth(0);

		// msg_send_user_id ?÷? ??????
		table.getColumn("msg_send_user_id").setWidth(0);
		table.getColumn("msg_send_user_id").setMinWidth(0);
		table.getColumn("msg_send_user_id").setMaxWidth(0);

		// ???? size ????
		table.getColumn("????").setPreferredWidth(250);
		
		// ?۽? List ?? ?ִ? ????, ù row ????
		int row=-1;
		if (table.getRowCount() !=0){
			//table.setRowSelectionInterval(0, 0);
			//row = table.getSelectedRow();
		}
		
		//((DefaultTableCellRenderer)table.getCellRenderer(1, 0)).setBackground(Color.black);
		//TableCellRenderer  renderer = table.getCellRenderer(1, 0);
		//Component component = table.prepareRenderer(renderer, 1, 0);
		//component.setBackground(Color.red);
		
		
		table.updateUI();
		
		// msg_send_id ?? column Index
		colIndexOfSendId = table.getColumn("msg_send_id").getModelIndex();
		
		if (row != -1){
			msg_send_id = (Integer)table.getValueAt(row, colIndexOfSendId);
		} else {
			msg_send_id = -1;
		}
		//System.out.println("msg_send_id ="+msg_send_id);
		
		// ????, ???? ?????ֱ?.
		//showMessage();		
		msg_send_id=-1;
		// ???? List ??ȸ
		recvListModel = new RecieveUserListModel(con, msg_send_id);
		tableList.setModel(recvListModel);
		tableList.setRowSorter(new TableRowSorter(recvListModel));

		// Ȯ?ο??? size ????
		tableList.getColumn("Ȯ?ο???").setPreferredWidth(20);
		// Ȯ?ο??? text ???? center
		DefaultTableCellRenderer  cellRender = new DefaultTableCellRenderer();
		cellRender.setHorizontalAlignment(JLabel.CENTER);
		tableList.getColumn("Ȯ?ο???").setCellRenderer(cellRender);

		// msg_recieve_id ?÷? ??????
		tableList.getColumn("msg_recieve_id").setWidth(0);
		tableList.getColumn("msg_recieve_id").setMinWidth(0);
		tableList.getColumn("msg_recieve_id").setMaxWidth(0);

		tableList.updateUI();
		
		// ?ű? ?۽? ?޼??? üũ thread
		threadFlag=true;
		thread = new Thread(this);
		thread.start();
	}
	
	public void setThreadFlag(boolean threadFlag){
		this.threadFlag = threadFlag;
		System.out.println("SendMessageList : threadFlag = "+this.threadFlag);
	}
	
	public void close(){
		this.treeMain.removeMenuOpenList(this);
		setThreadFlag(false);
		dispose();
	}
	
	public void search(String searchType){
		/*
		 * searchType : I - ?Է? ??ȸ   (?˻???ư?̳? ?Է? ?Ͽ? ??ȸ?ϴ? ????)
		 *                     C - üũ ??ȸ  (checkNewSendMsg() ???? ??ȸ?Ǵ? ????)
		 */
		if (searchType.equals("I")){
			table.clearSelection();
		}
		//System.out.println("search");
		String srch = t_input.getText();
		model.getList(userId, srch);
		table.updateUI();
		
		if (searchType.equals("I") && table.getRowCount()!=0){
			table.setRowSelectionInterval(0, 0);
		}		
		
	}
	
	// ????, ???? ?????ֱ?
	public void showMessage(){
		
		int col;
		int row=-1;
		t_title.setText("");
		area.setText("");
		
		if (table.getRowCount()!=0){
			row = table.getSelectedRow();
		}
		//System.out.println("showMessage : sel row = "+row);
		if (row != -1){		
			// title
			col =  table.getColumn("????").getModelIndex();
			//System.out.println("????="+col);
			String title = (String)table.getValueAt(row, col);
			t_title.setText(title);
			
			// content
			col =  table.getColumn("msg_send_content").getModelIndex();
			//System.out.println("????="+col);
			String content = (String)table.getValueAt(row, col);
			area.setText(content);
		}
		
	}
	
	// ???õ? ?۽? ?޼????? ?????? ????Ʈ ??ȸ
	public void showRecvList(){
		int row=-1;
		if (table.getRowCount()!=0){
			row=table.getSelectedRow();
		}
		if (row!=-1){
			msg_send_id = (Integer)table.getValueAt(row, colIndexOfSendId);
		} else {
			msg_send_id = -1;
		}

		//System.out.println("showRecvList : msg_send_id="+msg_send_id);
		recvListModel.getList(msg_send_id);
		tableList.updateUI();
	}
	
	public void checkNewSendMsg(){
		
		// threadFlag ?? false ?϶??? ???????? ?ʴ´?.
		if (threadFlag==false)  return;
		
		PreparedStatement pstmt=null;
		ResultSet  rs=null;
		StringBuffer sql=new StringBuffer();
		int max_msg_send_id=-1; // ???? ?۽? ?޼????? msg_send_id
		int currSelRow=table.getSelectedRow(); // ???? ???õ? row
		int currSelMsgSendId=-1; // ???? ???õ? msg_send_id
		
		// ???? ?۽? ?޼??? id check
		sql.append(" select nvl(max(s.msg_send_id),-1) max_msg_send_id \n");
		sql.append(" from   send_message s \n");
		sql.append(" where s.msg_send_user_id = ? \n");		
		try {
			pstmt=con.prepareStatement(sql.toString());
			pstmt.setString(1, userId);
			rs = pstmt.executeQuery();
			if (rs.next()){
				max_msg_send_id = rs.getInt("max_msg_send_id");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (rs!=null)
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			if (pstmt!=null)
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
		
		// ???? ?޼????? ???? ????(?????Ͱ? ???? ????) ???? ?ߴ?
		if (max_msg_send_id==-1) return;
		
		// ???? ?޼????? ???? ??ȸ?? table ?? ?????ϴ?ġ üũ
		boolean existFlag=false;
		for (int i=0; i<table.getRowCount(); i++){
			int msg_send_id = (Integer)table.getValueAt(i, colIndexOfSendId);
			if (msg_send_id==max_msg_send_id){
				existFlag=true;
				break;
			}
		}
		
		// ???? table ?? ??ȸ?Ǿ? ?ִ? ?????? ?????? List ?? ????ȸ. Ȯ?ο??? check ????.
		if (existFlag) {
			// ?????? List
			showRecvList();
			return;
		}
		
		// ???? ???õ? row ?? ?ִ? ????, ?ش? row ?? msg_send_id ?? üũ
		if (currSelRow!=-1){
			currSelMsgSendId = (Integer)table.getValueAt(currSelRow, colIndexOfSendId);
		}
		
		// ????ȸ
		search("C");
		
		// ???õǾ??? msg_send_id ã?Ƽ? ?????? ?ش?.
		int chk_msg_send_id;
		int newSelRow=-1;
		for (int i=0; i<table.getRowCount(); i++){
			chk_msg_send_id=(Integer)table.getValueAt(i, colIndexOfSendId);
			if (chk_msg_send_id==currSelMsgSendId){
				newSelRow=i;
				break;
			}
		}
		
		// ???õǾ??? row ?? ?־??? ????, ?ش? row ????
		if (newSelRow!=-1){
			table.setRowSelectionInterval(newSelRow, newSelRow);
		}
		
		// ????, ???? ?????ֱ?
		showMessage();
					
		// ?????? List
		showRecvList();

	}
	
	public void run() {
		while (threadFlag){
			try {
				//System.out.println("t_input.isEditable = "+t_input.isEditable());
				thread.sleep(1000);
				checkNewSendMsg();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		Object obj = e.getSource();
		if (obj==bt_search){
			
			// ?۽? ?޼??? ????Ʈ ??ȸ
			search("I");
			
			// ????, ???? ?????ֱ?
			showMessage();
			
			// ?????? List
			showRecvList();
		}		
	}

}
