package com.br.run;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import com.br.view.MemberView;

public class MemberProgram {

	public static void main(String[] args) {
		
		new MemberView().mainMenu();

	}

}
