package com.br.model.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.br.model.vo.Member;

/*
 * Dao (Data Access Object)
 * DB와 직접적으로 접근해서 sql문 실행 결과 받기 (JDBC과정)
 * 결과를 Controller로 반환
 */

public class MemberDao {
	
	/*
	 * < Statement, PreparedStatement >
	 * 1. sql문을 실행하고 결과를 받아내는 객체 (둘 중 하나 이용 )
	 * 2. Statement가 PreparedStatement 의 부모 (상속 구조)
	 * 3. 차이점
	 * 		ㄴ Statement는 "완성형태의 sql문"을 전달하면서 곧바로 실행
	 * 		ㄴ PreparedStatement는 "미완성형태의 sql문"을 임시로 가지고 있다가 완성시킨 후 실행 
	 * 			> 미완성형태 : 실제 데이터값이 채워질 공간을 ?(홀더)로 공간을 확보해둔 상태로 작성해둔 형태
	 * 						   ex) insert into 테이블 values(?, ?, ?)
	 * 
	 * < Statement 방식 >
	 * 1) Connection 객체를 통해 Statement 객체 생성
	 * 		stmt = conn.createStatement();
	 * 2) sql문을 전달하면서 실행 후 결과받기
	 * 		결과 = stmt.executeXXX(완성된sql문);
	 * 
	 * < PreparedStatement 방식 >
	 * 1) Connection 객체를 통해 PreparedStatement 객체 생성
	 * 		pstmt = conn.prepareStstement([미]완성된sql문); 	// sql문을 담으면서 생성
	 * 2) PreparedStatement에 담긴 sql문이 미완성된 상태일 경우 완성시키기
	 * 		pstmt.setXXX(홀더순번, 채울값);
	 * 3) sql문 실행 후 결과받기
	 * 		결과 = pstmt.executeXXX();		// sql문을 전달 할 필요 없음
	 * 
	 */
	
	
	
	
	/**
	 * 사용자가 요청한 회원 전체 조회를 처리해주는 메소드
	 * @return 	텅빈리스트 | 조회결과가 담긴 리스트
	 */
	
	public List<Member> selectMemberList() { // 전체 조회용 메소드
		
		// select문(여러행 조회) => ResultSet객체 => 각 행들은 Member객체 => 리스트에 쌓기
		
		// 필요한 변수들 미리 세팅
		List<Member> list = new ArrayList<>(); // 텅빈리스트
		Connection conn = null;
		Statement stmt = null;
		ResultSet rset = null;
		
		// 실행할 sql문
		String sql = "SELECT * FROM MEMBER";
		
		try {
			// 1) jdbc driver 등록
			Class.forName("oracle.jdbc.driver.OracleDriver");
			// 2) Connection 생성
			conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", "JDBC", "JDBC");
			// 3) Statement 생성
			stmt = conn.createStatement();
			// 4, 5) sql문 실행 후 결과받기
			rset = stmt.executeQuery(sql);
			// 6) 조회된 데이터값 뽑아서 자바 객체로 담기
			//    한 행의 컬럼값들 => Member객체 생성 후 필드에 담기 => list에 추가
			while(rset.next()) { // 반복적으로 모든 행을 스캔
				list.add(new Member(rset.getInt("user_no")
								  , rset.getString("user_id")
								  , rset.getString("user_pwd")
								  , rset.getString("user_name")
								  , rset.getString("gender")
								  , rset.getInt("age")
								  , rset.getString("email")
								  , rset.getString("phone")
								  , rset.getString("hobby")
								  , rset.getDate("regist_date")
								  ));
			}
					
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				// 7) 자원반납
				rset.close();
				stmt.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return list; // 조회결과가없었을경우 텅빈리스트 | 조회결과가있었을경우 뭐라도담긴리스트
		
	}
	
	/**
	 * 사용자가 입력한 정보들을 추가시켜주는 메소드
	 * @param m(매개변수, 파라미터 약자) 사용자가 입력한 값들이 각 필드에 담겨있는 Member객체
	 * @return insert 후에 처리된 행 수(추가 된 행 수)
	 */
	public int insertMember(Member m) { // 신규회원 추가해주는 메소드
		
		// insert문 => 처리된 행수 (int) => 트랜잭션 처리
		
		int result = 0;		// 처리된 결과(행수)를 받아줄 변수
		Connection conn = null; // DB연결정보를 담는 객체
		PreparedStatement pstmt = null;
		
		String sql = "INSERT INTO MEMBER VALUES(SEQ_UNO.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ? , SYSDATE)";
		
		try {
		Class.forName("oracle.jdbc.driver.OracleDriver");
		conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", "JDBC", "JDBC");
		pstmt = conn.prepareStatement(sql); // sql문을 담은채로 생성 (하필 미완성된 sql문)
		
		// ? 자리에 실제 데이터값 채우기
		// pstmt.setString(홀더순번, 대체할값)		=> '대체할값' - 문자타입으로 알아서 채워짐
		// pstmt.setInt(홀더순번, 대체할값)			=>  대체할값
		pstmt.setString(1, m.getUserId());
		pstmt.setString(2, m.getUserPwd());
		pstmt.setString(3, m.getUserName());
		pstmt.setString(4, m.getGender());
		pstmt.setInt(5, m.getAge());
		pstmt.setString(6, m.getEmail());
		pstmt.setString(7, m.getPhone());
		pstmt.setString(8, m.getHobby());
		
		result = pstmt.executeUpdate();
		
		if(result > 0) {
			conn.commit();
		}else {
			conn.rollback();
		}
		
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
			try {
			pstmt.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		
		}
		
	}
		
		return result;
}
	
	/**
	 * 사용자가 입력한 아이디로 검색 요청 처리해주는 메소드
	 * @param userId 	사용자가 입력한 검색하고자 하는 회원 아이디
	 * @return	null | 조회데이터가 담겨있는 Member객체
	 */
	
	public Member selectMemberByUserId(String userId) { // ID 검색 서비스
		// select문(한행) => ResultSet객체 => Member객체
		
		Member m = null;		// 최종 조회결과를 담아낼 변수
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		
		// 실행할 sql문 (완성형태로)
		// SELECT * FROM MEMBER WHERE USER_ID = 'XXXXX'
		String sql = "SELECT * FROM MEMBER WHERE USER_ID = ?";
		
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", "JDBC", "JDBC");
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userId);
			rset = pstmt.executeQuery();
			
			if(rset.next()) {
				m = new Member(rset.getInt("user_no")
						 , rset.getString("user_id")
						 , rset.getString("user_pwd")
						 , rset.getString("user_name")
						 , rset.getString("gender")
						 , rset.getInt("age")
						 , rset.getString("email")
						 , rset.getString("phone")
						 , rset.getString("hobby")
						 , rset.getDate("regist_date"));
			}
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				rset.close();
				pstmt.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return m;	// null | 조회결과가담긴Member객체
		
	}
	
	
	/**
	 * 사용자가 입력한 이름으로 키워드 검색 요청 처리해주는 메소드
	 * @param userName 검색하고자 하는 회원 이름
	 * @return 텅빈리스트 | 조회 결과가 담긴 리스트
	 */
	
	public List<Member> selectMemberByUserName(String userName) { // 이름으로 키워드 검색
		// select문(여러행) => ResultSet객체 => 각 행 Member객체 => 리스트에 쌓기
		
		List<Member> list = new ArrayList<>();  // 텅빈리스트
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		
		String sql = "SELECT * FROM MEMBER WHERE USER_NAME LIKE = ?";
		
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", "JDBC", "JDBC");
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userName);
			rset = pstmt.executeQuery();

			while(rset.next()) {
				list.add(new Member(rset.getInt("user_no")
								  , rset.getString("user_id")
								  , rset.getString("user_pwd")
								  , rset.getString("user_name")
								  , rset.getString("gender")
								  , rset.getInt("age")
								  , rset.getString("email")
								  , rset.getString("phone")
								  , rset.getString("hobby")
								  , rset.getDate("regist_date")));
			}
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				rset.close();
				pstmt.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return list;
		
	}
	
	
	/**
	 * 사용자가 입력한 정보로 변경 요청 처리해주는 메소드
	 * @param m	수정할 회원아이디, 수정할 정보가 담겨있는 Member 객체
	 * @return  update 후 처리 된 행 수
	 */
	
	public int updateMember(Member m) { // 특정 회원 찾아서 정보 수정
		
		// update문 => 처리된 행수(int) => 트랜잭션 처리
		int result = 0;
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = "UPDATE MEMBER "
				+   "SET USER_PWD = ?"
				+     ", EMAIL = ?"
				+	  ", PHONE = ?"
				+	  ", HOBBY = ?"
				+ "WHERE USER_ID = ?";
		
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", "JDBC", "JDBC");
			pstmt = conn.prepareStatement(sql);
			
			pstmt.setString(1, m.getUserPwd());
			pstmt.setString(2, m.getEmail());
			pstmt.setString(3, m.getPhone());
			pstmt.setString(4, m.getHobby());
			pstmt.setString(5, m.getUserId());
			
			result = pstmt.executeUpdate();
			
			if(result > 0) {
				conn.commit();
			}else {
				conn.rollback();
			}
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				pstmt.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return result;
		}
	

	
	
	/**
	 * 사용자가 입력한 아이디값 전달 받아서 회원 탈퇴시켜주는 메소드
	 * @param userId 탈퇴시키고자 하는 회원 아이디 값
	 * @return	delete 후 처리된 행 수
	 */
	
	public int deleteMember(String userId) { // 아이디 전달받고 회원 탈퇴
		// delete 문 => 처리된 행수 => 트랜잭션 처리
		
		// DELETE FROM MEMBER WHERE USER_ID = 'XXXX'
		

		int result = 0;
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = "DELETE FROM MEMBER WHERE USER_ID = ?";
		
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", "JDBC", "JDBC");
			pstmt = conn.prepareStatement(sql); //미완성된 sql문
			
			pstmt.setString(1, userId);
			
			result = pstmt.executeUpdate();
			
			if(result > 0) {
				conn.commit();
			}else {
				conn.rollback();
			}
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				pstmt.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return result;
		
	}	

	
	
	

}
