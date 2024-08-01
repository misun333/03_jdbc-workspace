package com.br.model.dao;

import static com.br.common.JDBCTemplate.close;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.br.model.vo.Member;

public class MemberDao {
	
	/*
	 * * 기존의방식  : 사용자가 요청할 때 마다 실행되는 sql문들이 자바소스코드내에 명시적으로 작성 (정적코딩방식)
	 * 	 ㄴ 문제점   : sql문을 수정해야 될 경우 자바코드를 수정해야됨 => 반영시키고자 할 경우 프로그램 재 구동
	 *   ㄴ 해결방식 : sql문들만 별도로 관리하는 외부파일(.xml)을 만들어서 "실시간"으로 
	 *   			   그 파일에 기록 된 sql문을 읽어들여서 실행하도록 (동적코딩방식)
	 * 
	 */
	
	private Properties prop = new Properties();
	
//	dao 측 메소드가 호출될 때마다 new Memberdao(). xxxxxxx(); 호출
//	즉, 기본 생성자 실행 후 메소드 실행
	public MemberDao() {
		
		try {
			prop.loadFromXML(new FileInputStream("resources/query.xml"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public List<Member> selectMemberList(Connection conn) {
		// select문 (여러행) => ResultSet객체 => 각행은 Member객체 => List<Member> 에 쌓기
		List<Member> list = new ArrayList<>();
		
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		
		String sql = prop.getProperty("selectMemberList");
		
		try {
			// 3) PreparedStatement 생성 
			pstmt = conn.prepareStatement(sql); // 애초에 완성된 sql문
			// 4, 5) sql문 실행 후 결과받기
			rset = pstmt.executeQuery();
			
			while(rset.next()) {
				// 한 행의 모든 컬럼값들 => Member객체의 각 필드에 대입 => 리스트에 추가
				list.add(new Member( rset.getInt("USER_NO")
								   , rset.getString("user_id")
								   , rset.getString("user_pwd")
								   , rset.getString("user_name")
								   , rset.getString("gender")
								   , rset.getInt("age")
								   , rset.getString("email")
								   , rset.getString("phone")
								   , rset.getString("hobby")
								   , rset.getDate("regist_date") )); 
			}
						
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			/*JDBCTemplate.*/close(rset);
			/*JDBCTemplate.*/close(pstmt);
			//JDBCTemplate.close(conn); => Service에서 반납
		}
		
		return list;
		
	}
	
	public int insertMember(Connection conn, Member m) {
		// insert문 => 처리된행수(int) => 트랜잭션처리(Service에서 진행)
		int result = 0;
		PreparedStatement pstmt = null;
		
		String sql = prop.getProperty("insertMember");
		
		try {
			pstmt = conn.prepareStatement(sql); // 3)  , 미완성된sql문
			
			pstmt.setString(1, m.getUserId());
			pstmt.setString(2, m.getUserPwd());
			pstmt.setString(3, m.getUserName());
			pstmt.setString(4, m.getGender());
			pstmt.setInt(5, m.getAge());
			pstmt.setString(6, m.getEmail());
			pstmt.setString(7, m.getPhone());
			pstmt.setString(8, m.getHobby());
			
			result = pstmt.executeUpdate(); // 4), 5)
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(pstmt);
		}
		
		return result;
	}
	
	public Member selectMemberByUserId(Connection conn, String userId) {
		// select문 (한행) => ResultSet객체 => Member객체
		Member m = null;
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		
		String sql = prop.getProperty("selectMemberByUserId");
		
		try {
			pstmt = conn.prepareStatement(sql); // 미완성된 sql문
			pstmt.setString(1, userId);
			
			rset = pstmt.executeQuery();
			
			if(rset.next()) {
				m = new Member( rset.getInt("user_no")
							  , rset.getString("user_id")
							  , rset.getString("user_pwd")
							  , rset.getString("user_name")
							  , rset.getString("gender")
							  , rset.getInt("age")
							  , rset.getString("email")
							  , rset.getString("phone")
							  , rset.getString("hobby")
							  , rset.getDate("regist_Date") );
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rset);
			close(pstmt);
		}
		
		return m;
	}
	
	
	// SELECT * FROM MEMBER WHERE USER_NAME LIKE '%' || ? || '%' 
	public List<Member> selectMemberByUserName(Connection conn, String userName){
		
		// select문 => ResultSet => List<Member>
		List<Member> list = new ArrayList<>();
		
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		
		String sql = prop.getProperty("selectMemberByUserName");
		
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userName);
			rset = pstmt.executeQuery();
			
			while(rset.next()) {
				list.add(new Member( rset.getInt("user_no")
								   , rset.getString("user_id")
								   , rset.getString("user_pwd")
								   , rset.getString("user_name")
								   , rset.getString("gender")
								   , rset.getInt("age")
								   , rset.getString("email")
								   , rset.getString("phone")
								   , rset.getString("hobby")
								   , rset.getDate("regist_date") ));	
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rset);
			close(pstmt);
		}
		
		return list;
		
	}
	
	// UPDATE MEMBER SET USER_PWD=?, EMAIL=?, PHONE=?, HOBBY=? WHERE USER_ID=?
	public int updateMember(Connection conn, Member m) {
		// update문 => 처리된행수(int) 
		int result = 0;
		PreparedStatement pstmt = null;
		String sql = prop.getProperty("updateMember");
		
		try {
			pstmt = conn.prepareStatement(sql); // 미완성된 sql문
			pstmt.setString(1, m.getUserPwd());
			pstmt.setString(2, m.getEmail());
			pstmt.setString(3, m.getPhone());
			pstmt.setString(4, m.getHobby());
			pstmt.setString(5, m.getUserId());
			
			result = pstmt.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(pstmt);
		}
		
		return result;
	}
	
	// DELETE FROM MEMBER WHERE USER_ID = ?
	public int deleteMember(Connection conn, String userId) {
		// delete문 => 처리된행수(int)
		int result = 0;
		PreparedStatement pstmt = null;
		
		String sql = prop.getProperty("deleteMember");
		
		try {
			pstmt = conn.prepareStatement(sql); // 미완성된 sql문
			pstmt.setString(1, userId);
			
			result = pstmt.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(pstmt);
		}
		
		return result;
		
	}
	
	
	public String loginMember(Connection conn, Map<String, String>map) {
		// SELECT * FROM MEMBER WHERE USER_ID = ? AND USER_PWD = ? 
		// select문(한 행, 하나의문자열) => ResultSet 객체 => String
		
		String loginUserName = null; // String 변수에 담고 null로 초기화
		
		PreparedStatement pstmt = null;
		ResultSet rset = null; // 무조건 ResultSet 담아줘야함
		
		String sql = prop.getProperty("loginMember");
		
		
		try {
			pstmt = conn.prepareStatement(sql); // 미완성된 sql문
			pstmt.setString(1, map.get("userId"));
			pstmt.setString(2, map.get("userPwd")); // 물음표 자리에 들어갈 값
			
			rset = pstmt.executeQuery(); // 한 행 조회. 조회결과가 돌아옴. 둘 중 하나만 맞으면 조회 결과 없음
			
			if(rset.next()) { // 조회 결과가 있을 때 진행
				loginUserName = rset.getString("user_name");
			}
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rset);
			close(pstmt);
		}
		
		return loginUserName;
		
	}
	
	public String selectOldMemberName(Connection conn) {
		String oldMemName = null;
		
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		
		String sql = prop.getProperty("selectOldMemberName");
		
		try {
			pstmt = conn.prepareStatement(sql);
			rset = pstmt.executeQuery();
			if(rset.next()) {
				oldMemName = rset.getString("user_name");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
			close(rset);
			close(pstmt);
		}
		return oldMemName;
	}
	
	
	public int selectMemberCount(Connection conn) {
		// select문 => ResultSet => int 
		
		int count = 0;
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		
		String sql = prop.getProperty("selectMemberCount");
		
		try {
			pstmt = conn.prepareStatement(sql);
			rset = pstmt.executeQuery();
			
			if(rset.next()) {
				count = rset.getInt("COUNT");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(rset);
			close(pstmt);
		}
		
		return count;
		
	}
	
	
	
	

}
