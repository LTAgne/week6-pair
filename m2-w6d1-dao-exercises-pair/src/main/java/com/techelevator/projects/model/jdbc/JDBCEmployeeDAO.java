package com.techelevator.projects.model.jdbc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.techelevator.projects.model.Department;
import com.techelevator.projects.model.Employee;
import com.techelevator.projects.model.EmployeeDAO;

public class JDBCEmployeeDAO implements EmployeeDAO {

	private JdbcTemplate jdbcTemplate;

	public JDBCEmployeeDAO(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	@Override
	public List<Employee> getAllEmployees() {
		List<Employee> employeeList = new ArrayList<>();
		String sqlGetEmployee = "SELECT * FROM employee";
		SqlRowSet results = jdbcTemplate.queryForRowSet(sqlGetEmployee);
		while(results.next()){
			employeeList.add(mapRowToEmployee(results));
		}
		return employeeList;
	}

	@Override
	public List<Employee> searchEmployeesByName(String firstNameSearch, String lastNameSearch) {
		List<Employee> employeeList = new ArrayList<>();
		String sqlGetEmployees = "SELECT * FROM employee WHERE first_name = ? AND last_name = ?";
		SqlRowSet results = jdbcTemplate.queryForRowSet(sqlGetEmployees, firstNameSearch, lastNameSearch);
		while(results.next()){
			employeeList.add(mapRowToEmployee(results));
		}
		return employeeList;
	}

	@Override
	public List<Employee> getEmployeesByDepartmentId(long id) {
		List<Employee> employeeList = new ArrayList<>();
		String sqlGetEmployeeById = "SELECT * FROM employee WHERE department_id = ?";
		SqlRowSet results = (jdbcTemplate.queryForRowSet(sqlGetEmployeeById, id));
		while(results.next()){
			employeeList.add(mapRowToEmployee(results));
		}
		return employeeList;
	}

	@Override
	public List<Employee> getEmployeesWithoutProjects() {
		List<Employee> employeeList = new ArrayList<>();
		String sqlGetEmployeeById = "SELECT * FROM employee e LEFT JOIN project_employee pe ON e.employee_id = pe.employee_id WHERE pe.employee_id IS NULL";
		SqlRowSet results = (jdbcTemplate.queryForRowSet(sqlGetEmployeeById));
		while(results.next()){
			employeeList.add(mapRowToEmployee(results));
		}
		return employeeList;
	}

	@Override
	public List<Employee> getEmployeesByProjectId(Long projectId) {
		List<Employee> employeeList = new ArrayList<>();
		String sqlGetEmployeeById = "SELECT * FROM employee e JOIN project_employee pe ON e.employee_id = pe.employee_id WHERE pe.project_id = ?";
		SqlRowSet results = (jdbcTemplate.queryForRowSet(sqlGetEmployeeById, projectId));
		while(results.next()){
			employeeList.add(mapRowToEmployee(results));
		}
		return employeeList;
	}

	@Override
	public void changeEmployeeDepartment(Long employeeId, Long departmentId) {
		String sqlUpdateDepartment = "UPDATE employee SET department_id = ? WHERE employee_id = ?";
		jdbcTemplate.update(sqlUpdateDepartment, departmentId, employeeId);
	}
	
	public Employee createEmployee(String firstName, String lastName, LocalDate birthDate, char gender, LocalDate hireDate) {
		Employee employee = new Employee();
		employee.setFirstName(firstName);
		employee.setLastName(lastName);
		employee.setBirthDay(birthDate);
		employee.setGender(gender);
		employee.setHireDate(hireDate);
		String sqlCreateEmployee = "INSERT INTO employee (first_name, last_name, birth_date, gender, hire_date) VALUES (?,?,?,?,?) RETURNING employee_id";
		employee.setId(jdbcTemplate.queryForObject(sqlCreateEmployee,Long.class, firstName, lastName, birthDate, gender, hireDate));
		return employee;
	}
	
	private Employee mapRowToEmployee(SqlRowSet results) {
		Employee theEmp = new Employee();
		theEmp.setId(results.getLong("employee_id"));
		theEmp.setFirstName(results.getString("first_name"));
		theEmp.setLastName(results.getString("last_name"));
		theEmp.setBirthDay(results.getDate("birth_date").toLocalDate());
		theEmp.setDepartmentId(results.getLong("department_id"));
		theEmp.setGender(results.getString("gender").charAt(0));
		theEmp.setHireDate(results.getDate("hire_date").toLocalDate());
		return theEmp;
	}


}
