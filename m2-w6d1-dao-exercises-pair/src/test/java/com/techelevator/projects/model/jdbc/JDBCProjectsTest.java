package com.techelevator.projects.model.jdbc;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import com.techelevator.projects.model.Department;
import com.techelevator.projects.model.Employee;
import com.techelevator.projects.model.Project;

public class JDBCProjectsTest {
	private static SingleConnectionDataSource dataSource;
	private JDBCEmployeeDAO daoEmp;
	private JDBCProjectDAO dao;
	JdbcTemplate jdbcTemplate;
	private Employee larry;
	private Employee leslie;
	private Employee lil;
	private Project project0;
	private Project project1;
	
	
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
		jdbcTemplate.update("DELETE FROM project_employee; DELETE FROM employee; DELETE FROM project;");
		daoEmp = new JDBCEmployeeDAO(dataSource);
		dao = new JDBCProjectDAO(dataSource);
		project0 = dao.createProject("Project 0", LocalDate.of(2000, 1, 1), LocalDate.of(2010, 1, 1));
		project1 = dao.createProject("Project 1", LocalDate.of(2000, 1, 1), LocalDate.of(2020, 1, 1));
		larry = daoEmp.createEmployee("Larry", "Gurgich", LocalDate.now(), 'M', LocalDate.now());
		leslie = daoEmp.createEmployee("Leslie", "Knope", LocalDate.now(), 'F', LocalDate.now());
		lil = daoEmp.createEmployee("Lil'", "Sebastian", LocalDate.now(), 'M', LocalDate.now());
	}

	/* After each test, we rollback any changes that were made to the database so that
	 * everything is clean for the next test */
	@After
	public void rollback() throws SQLException {
		dataSource.getConnection().rollback();
	}

	@Test
	public void testGetAllActiveProjects() {
		List<Project> projects = dao.getAllActiveProjects();
			assertEquals(projects.get(0).getName(), "Project 1");
	}

	@Test
	public void testRemoveEmployeeFromProject() {
		dao.addEmployeeToProject(project1.getId(), larry.getId());
		assertEquals(daoEmp.getEmployeesByProjectId(project1.getId()).get(0).getId(),larry.getId());
		dao.removeEmployeeFromProject(project1.getId(), larry.getId());
		assertEquals(daoEmp.getEmployeesByProjectId(project1.getId()).size(), 0);
	}

	@Test
	public void testAddEmployeeToProject() {
		dao.addEmployeeToProject(project1.getId(), larry.getId());
		assertEquals(daoEmp.getEmployeesByProjectId(project1.getId()).size(), 1);
		assertEquals(daoEmp.getEmployeesByProjectId(project1.getId()).get(0).getId(),larry.getId());
	}

}
