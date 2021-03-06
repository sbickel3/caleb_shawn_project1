package revature.project1.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import revature.project1.models.Log4JTest;
import revature.project1.models.Users;
import revature.project1.utils.ConnectionFactory;

public class UserDAOImpl implements UserDAO {
	
	final static Logger logger = Logger.getLogger(Log4JTest.class);
	
	@Override
	public Users login(String username, String password) {
		
		Users existingUser = null;

		try (Connection conn = ConnectionFactory.getInstance().getConnection();) {
			conn.setAutoCommit(false);

			// determine if the account exists; if no rows were affected by executeUpdate,
			// then the account does not exist
			String sqlExistingAccount = "select * from ers_users where ers_username = ? AND ers_password = ?";
			PreparedStatement pstmtExisting = conn.prepareStatement(sqlExistingAccount);
			pstmtExisting.setString(1, username);
			pstmtExisting.setString(2, password);
			

			ResultSet loginResultSet = pstmtExisting.executeQuery(); // execute the query and retrieve the result of the
																		// query
			System.out.println(loginResultSet);
			// if the user doesn't exist, return null
			if (!loginResultSet.isBeforeFirst()) {
				return existingUser;
			}

			/*
			 * since the user exists, retrieve the column values of the selected record from
			 * the ers_users table and set the private fields of the new user
			 */
			existingUser = new Users();
			if (loginResultSet.next()) {
				existingUser.setUserId(loginResultSet.getInt("ers_users_id"));
				existingUser.setFirstName(loginResultSet.getString("user_first_name"));
				existingUser.setLastName(loginResultSet.getString("user_last_name"));
				existingUser.setUsername(loginResultSet.getString("ers_username"));
				existingUser.setPassword(loginResultSet.getString("ers_password"));
				existingUser.setEmail(loginResultSet.getString("user_email"));
				existingUser.setUserRoleId(loginResultSet.getInt("user_role_id"));
			}
			
			conn.commit();
			conn.close();
		} catch (SQLException e) {
			logger.error("SQL Exception : " + e);
		}
		return existingUser;
	}

	@Override
	public int getUserId(String username) {
		int userId = -1;
		try (Connection conn = ConnectionFactory.getInstance().getConnection();) {
			conn.setAutoCommit(false);
			String getId = "SELECT ers_users_id FROM ers_users WHERE ers_username = ?";
			PreparedStatement pstmtgetId = conn.prepareStatement(getId);
			pstmtgetId.setString(1, username);
			ResultSet rs = pstmtgetId.executeQuery();
			if (!rs.next()) {
				return userId;
			}
			userId = rs.getInt("ers_users_id");
			conn.commit();
			conn.close();

		} catch (SQLException e) {
			logger.error("SQL Exception : " + e);
		}

		return userId;
	}

	@Override
	public boolean createUser(String username, String password, String firstname, String lastname, String email,
			int roleId) {
		try (Connection conn = ConnectionFactory.getInstance().getConnection();) {
			conn.setAutoCommit(false);

			// determine if the username or email is unique
			String sqlRetrieveUser = "select * from ers_users where ers_username = ? OR user_email = ?";
			PreparedStatement pstmtUsernames = conn.prepareStatement(sqlRetrieveUser);
			pstmtUsernames.setString(1, username);
			pstmtUsernames.setString(2, email);
			ResultSet checkIfUserExists = pstmtUsernames.executeQuery();

			// if the user already exists, return false
			if (checkIfUserExists.next()) {
				return false;
			}

			// since the account does not already exist, a new user is inserted into the
			// ers_users table
			String sqlAddUser = "insert into ers_users (ers_username, ers_password, user_first_name, user_last_name, user_email, user_role_id) values(?, ?, ?, ?, ?, ?)";

			PreparedStatement pstmtUser = conn.prepareStatement(sqlAddUser);
			pstmtUser.setString(1, username);
			pstmtUser.setString(2, password);
			pstmtUser.setString(3, firstname);
			pstmtUser.setString(4, lastname);
			pstmtUser.setString(5, email);
			pstmtUser.setInt(6, roleId);

			int rowsInsertedUser = pstmtUser.executeUpdate(); // executes the DML statement; inserts a new User into the
																// Users table; returns the number of rows affected

			if (rowsInsertedUser == 0) {
				return false;
			}

			conn.commit();
			conn.close();

		} catch (SQLException e) {
			logger.error("SQL Exception : " + e);
		}

		return true;
	}

	public boolean checkIfUserExistsById(int userId) {
		try (Connection conn = ConnectionFactory.getInstance().getConnection();) {
			conn.setAutoCommit(false);
			String getId = "SELECT * FROM ers_users WHERE ers_users_id = ?";
			PreparedStatement pstmtgetId = conn.prepareStatement(getId);
			pstmtgetId.setInt(1, userId);
			ResultSet rs = pstmtgetId.executeQuery();
			if (!rs.next()) {
				return false;
			}

			conn.commit();
			conn.close();

		} catch (SQLException e) {
			logger.error("SQL Exception : " + e);
		}

		return true;
	}

	public boolean isUsernameAvailable(String username) {

		try (Connection conn = ConnectionFactory.getInstance().getConnection();) {
			String getUsername = "SELECT ers_username FROM ers_users WHERE ers_username = ?";
			PreparedStatement pstmt = conn.prepareStatement(getUsername);
			pstmt.setString(1, username);
			
			ResultSet rs = pstmt.executeQuery();
			
			// if the query returns a result, the username is not available
			if (rs.next()) {
				conn.close();
				return false;
			}

			conn.close();

		} catch (SQLException e) {
			logger.error("SQL Exception : " + e);
		}

		return true;

	}

	public boolean isEmailAvailable(String emailAddress) {

		try (Connection conn = ConnectionFactory.getInstance().getConnection();) {
			String getEmail = "SELECT user_email FROM ers_users WHERE user_email = ?";
			PreparedStatement pstmt = conn.prepareStatement(getEmail);
			pstmt.setString(1, emailAddress);
			
			ResultSet rs = pstmt.executeQuery();
			
			// if the query returns a result, the email is not available
			if (rs.next()) {
				conn.close();
				return false;
			}

			conn.close();

		} catch (SQLException e) {
			logger.error("SQL Exception : " + e);
		}

		return true;

	}
}
