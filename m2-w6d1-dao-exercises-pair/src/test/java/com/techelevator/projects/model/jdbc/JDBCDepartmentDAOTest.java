package com.techelevator.projects.model.jdbc;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.techelevator.projects.model.Department;

public class JDBCDepartmentDAOTest {
	
	private static SingleConnectionDataSource dataSource;
	private JDBCDepartmentDAO dao;
	JdbcTemplate jdbcTemplate;
	
	@BeforeClass
	public static void setupDataSource() {
		dataSource = new SingleConnectionDataSource();
		dataSource.setUrl("jdbc:postgresql://localhost:5432/projects");
		dataSource.setUsername("postgres");
		dataSource.setPassword("postgres1");
		/* The following line disables autocommit for connections 
		 * returned by this DataSource. This allows us to rollback
		 * any changes after each test */
		dataSource.setAutoCommit(false);
	}
	
	/* After all tests have finished running, this method will close the DataSource */
	@AfterClass
	public static void closeDataSource() throws SQLException {
		dataSource.destroy();
	}
	
	@Before
	public void setup() {
		jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update("DELETE FROM project_employee; DELETE FROM employee; DELETE FROM department;");
		
		dao = new JDBCDepartmentDAO(dataSource);
	}

	/* After each test, we rollback any changes that were made to the database so that
	 * everything is clean for the next test */
	@After
	public void rollback() throws SQLException {
		dataSource.getConnection().rollback();
	}

	@Test
	public void testGetAllDepartments() {
		dao.createDepartment("Fake Department 0");
		dao.createDepartment("Fake Department 1");
		dao.createDepartment("Fake Department 2");
		
		List<Department> departmentList = dao.getAllDepartments();
		
		assertEquals("Incorrect number of departments returned", departmentList.size(), 3);
		for(int i = 0; i < 3; i++){
			assertEquals("Not all departments returned", departmentList.get(i).getName(), "Fake Department " + i);  //#creativity  #tuesdays
		}
	}

	@Test
	public void testSearchDepartmentsByName() {
		dao.createDepartment("Fake Department 0");
		dao.createDepartment("Fake Department 1");
		dao.createDepartment("Fake Department 2");
		
		List<Department> departmentList = dao.searchDepartmentsByName("0");
		
		assertEquals("Incorrect number of departments returned", departmentList.size(), 1);
		assertEquals("Incorrect department returned", departmentList.get(0).getName(), "Fake Department 0");
		
		departmentList = dao.searchDepartmentsByName("Fake Department");
		assertEquals("Incorrect number of departments returned", departmentList.size(), 3);
		for(int i = 0; i < 3; i++){
			assertEquals("Incorrect department returned", departmentList.get(i).getName(), "Fake Department " + i); 
		}
	}

	@Test
	public void testUpdateDepartmentName() {
		String departmentName = "MY_NEW_TEST_DEPARTMENT";
		Department newDept = dao.createDepartment(departmentName);
		
		assertEquals(departmentName, newDept.getName());
		
		dao.updateDepartmentName(newDept.getId(), departmentName + "_2.0");
		SqlRowSet results = jdbcTemplate.queryForRowSet("SELECT * FROM department");
		assertTrue("There were no departments in the database", results.next());
		assertEquals("The departement name was not correct", departmentName + "_2.0", results.getString("name"));
	}

	@Test
	public void testCreateDepartment() {
		String departmentName = "MY_NEW_TEST_DEPARTMENT";
		Department newDept = dao.createDepartment(departmentName);
		
		assertNotNull(newDept);
		SqlRowSet results = jdbcTemplate.queryForRowSet("SELECT * FROM department");
		assertTrue("There were no departments in the database", results.next());
		assertEquals(departmentName, results.getString("name"));
		assertEquals(newDept.getId(), (Long)results.getLong("department_id"));
		assertFalse("Too many rows", results.next());
	}

	@Test
	public void testGetDepartmentById() {
		String departmentName = "Fake Department";
		Department newDept = dao.createDepartment(departmentName);
		
		assertEquals(dao.getDepartmentById(newDept.getId()).getName(), newDept.getName());
	}

}
