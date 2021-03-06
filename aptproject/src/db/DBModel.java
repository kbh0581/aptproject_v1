package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import dto.Unit;

/*
 * 상속받을 클래스에서 사용해야 하는 method	
	
	생성자에서 init 호출
	Constructor {
		init(colName, sql);
	}
	
	바인드 변수 연결
	protected void setSQL() throws SQLException {
		pstmt = conn.prepareStatement(sql);
	}
	
	DTO와 연결
	protected void setTable(ResultSet rs) throws SQLException {
		arrList.clear();
		while (rs.next()) {
			DTO dto = new DTO();
			dto.set---(rs.getString(---));
			arrList.add(dto);
		}
	}
 
 */

public abstract class DBModel {
	protected Connection conn;
	protected PreparedStatement pstmt;
	protected ResultSet rs;

	protected String colName[];
	protected String sql;
	protected ArrayList arrList = new ArrayList();

	protected void init(String[] colName, String sql) {
		this.colName = colName;
		this.sql = sql;
		setData();
	}

	// select 와 insert, update, delete 구분
	private void setData() {
		String[] sqlArr = sql.split("\\s");
		if (sqlArr[0].equals("select")) {
			exeQuery();
		} else {
			exeUpdate();
		}
	}

	// 바인드 변수관련 설정을 하는 메서드
	// 바인드 변수를 사용하려면 오버라이딩 해야함
	protected void setSQL() throws SQLException {
		pstmt = conn.prepareStatement(sql);
	}

	// dto로 부터 데이터를 받아오는 메서드
	protected abstract void setTable(ResultSet rs) throws SQLException;

	private void exeQuery() {
		try {
			setSQL();
			rs = pstmt.executeQuery();
			// 각 테이블에 맞춰 setTable 메서드 수정 (상속받은 클래스에서)
			setTable(rs);

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			disconnect();
		}
	}

	private void exeUpdate() {
		try {
			setSQL();
			int res = pstmt.executeUpdate();
			if (res != 0) {
				System.out.println("쿼리실행성공");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			disconnect();
		}
	}

	// resultset과 preparedstatement 연결을 닫는 메서드
	private void disconnect() {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if (pstmt != null) {
			try {
				pstmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public String[] getColName() {
		return colName;
	}

	public ArrayList getData() {
		return arrList;
	}

}
