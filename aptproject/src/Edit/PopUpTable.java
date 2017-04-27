package Edit;

import java.awt.ScrollPane;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import viewer.AdminModel;

public class PopUpTable extends JFrame{
	
	
	
	JScrollPane sp;
	JTable ta;
	AdminModel ad;
	Connection con;
	RetunPan rp;
	int clickCount=-1;
	int selectrow;
	
	public PopUpTable(RetunPan rp,Connection con) {
		this.con=con;
		this.rp=rp;
		String where="";
		
		
		String name=rp.userList.get(0).getAptuser_id();
		
		//System.out.println(rp.userList.get(0).getAptuser_perm());
		
			where=" where aptuser_id="+"'"+name+"'";
	
		
		String sql="select invoice_id as 송장ID, invoice_barcode as 송장바코드, invoice_arrtime as 등록시간, invoice_taker as 수령인, invoice_taketime as 수령시간, invoice_takeflag as 수령여부, aptuser_id as 회원ID "
				+" from view_acis "+where;
		

		ad=new AdminModel(con);
		ad.getList(sql);
		ta=new JTable(ad);
		sp=new JScrollPane(ta);
		
		add(sp);
		
		ta.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				JTable a=(JTable)e.getSource();
				
				
				if(selectrow==a.getSelectedRow()&&clickCount==1){
				
					String value=ad.getValueAt(a.getSelectedRow(), 0).toString();
					int result=JOptionPane.showConfirmDialog(rp, "선택한 송장번호\n"+value+"가 맞습니까?",
							"송장번호 선택",JOptionPane.YES_NO_OPTION);
					
					if(result==0){
						
						rp.tf_id.setText(value);
						setVisible(false);
					}else{
						return;
					}
				}
				else if(clickCount==0){
					
					selectrow=a.getSelectedRow();
					clickCount=1;
				}
				else{
					clickCount=0;
					selectrow=-1;
				}
				
				
				
				
				
				
				
			}
		});
		
		setLocationRelativeTo(rp);
		
	
		setBounds(505, 860, 900, 110);
	
	}

	
}
