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

public class JDBCEmployeeDAOTest {

	private static SingleConnectionDataSource dataSource;
	private JDBCEmployeeDAO dao;
	private JDBCDepartmentDAO daoDep;
	private JDBCProjectDAO daoProj;
	JdbcTemplate jdbcTemplate;
	private Employee larry;
	private Employee leslie;
	private Employee lil;
	private Department department0;
	private Department department1;
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
		jdbcTemplate.update("DELETE FROM project_employee; DELETE FROM employee; DELETE FROM project; DELETE FROM department");
		dao = new JDBCEmployeeDAO(dataSource);
		daoDep = new JDBCDepartmentDAO(dataSource);
		daoProj = new JDBCProjectDAO(dataSource);
		department0 = daoDep.createDepartment("Department 0");
		department1 = daoDep.createDepartment("Department 1");
		project0 = daoProj.createProject("Project 0", LocalDate.now(), LocalDate.now());
		project1 = daoProj.createProject("Project 1", LocalDate.now(), LocalDate.now());
		larry = dao.createEmployee("Larry", "Gurgich", LocalDate.now(), 'M', LocalDate.now());
		leslie = dao.createEmployee("Leslie", "Knope", LocalDate.now(), 'F', LocalDate.now());
		lil = dao.createEmployee("Lil'", "Sebastian", LocalDate.now(), 'M', LocalDate.now());
	}

	/* After each test, we rollback any changes that were made to the database so that
	 * everything is clean for the next test */
	@After
	public void rollback() throws SQLException {
		dataSource.getConnection().rollback();
	}


	@Test
	public void testGetAllEmployees() {
		
		List<Employee> employeeList = dao.getAllEmployees();
		
		assertEquals("Incorrect number of employees returned", employeeList.size(), 3);

		assertEquals(employeeList.get(0).getFirstName(), "Larry");
		assertEquals(employeeList.get(1).getFirstName(), "Leslie");
		assertEquals(employeeList.get(2).getFirstName(), "Lil'");

	}

	@Test
	public void testSearchEmployeesByName() {
		dao.createEmployee("Larry", "Gurgich", LocalDate.now(), 'M', LocalDate.now());
		List<Employee> employeeList = dao.searchEmployeesByName("Lil'", "Sebastian");
		assertEquals(1, employeeList.size());
		assertEquals("Lil'", employeeList.get(0).getFirstName());
		
		employeeList = dao.searchEmployeesByName("Larry", "Gurgich");
		assertEquals(2, employeeList.size());
	}

	@Test
	public void testGetEmployeesByDepartmentId() {
		dao.changeEmployeeDepartment(leslie.getId(), department0.getId());
		assertEquals(dao.getEmployeesByDepartmentId(department0.getId()).size(), 1);
		assertEquals(dao.getEmployeesByDepartmentId(department0.getId()).get(0).getFirstName(),"Leslie");
		dao.changeEmployeeDepartment(lil.getId(), department0.getId());
		assertEquals(dao.getEmployeesByDepartmentId(department0.getId()).size(), 2);
		assertEquals(dao.getEmployeesByDepartmentId(department0.getId()).get(0).getFirstName(),"Leslie");
		assertEquals(dao.getEmployeesByDepartmentId(department0.getId()).get(1).getFirstName(),"Lil'");
	}

	@Test
	public void testGetEmployeesWithoutProjects() {
		daoProj.addEmployeeToProject(project0.getId(), larry.getId());
		daoProj.addEmployeeToProject(project1.getId(),leslie.getId());
		assertEquals(dao.getEmployeesWithoutProjects().get(0).getId(),lil.getId());
	}

	@Test
	public void testGetEmployeesByProjectId() {
		daoProj.addEmployeeToProject(project0.getId(), larry.getId());
		daoProj.addEmployeeToProject(project1.getId(),leslie.getId());
		assertEquals(dao.getEmployeesByProjectId(project0.getId()).get(0).getId(),larry.getId());
		assertEquals(dao.getEmployeesByProjectId(project1.getId()).get(0).getId(),leslie.getId());
	}

	@Test
	public void testChangeEmployeeDepartment() {
		dao.changeEmployeeDepartment(leslie.getId(), department0.getId());
		assertEquals((Long)dao.getAllEmployees().get(2).getDepartmentId(), department0.getId());
	}

}
