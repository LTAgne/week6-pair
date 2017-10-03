package com.techelevator.projects.model.jdbc;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import com.techelevator.projects.model.Department;
import com.techelevator.projects.model.DepartmentDAO;

public class JDBCDepartmentDAO implements DepartmentDAO {
	
	private JdbcTemplate jdbcTemplate;

	public JDBCDepartmentDAO(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public List<Department> getAllDepartments() {
		List<Department> departmentList = new ArrayList<>();
		String sqlGetDepartments = "SELECT * FROM department";
		SqlRowSet results = jdbcTemplate.queryForRowSet(sqlGetDepartments);
		while(results.next()){
			departmentList.add(mapRowToDept(results));
		}
		return departmentList;
	}

	@Override
	public List<Department> searchDepartmentsByName(String nameSearch) {
		List<Department> departmentList = new ArrayList<>();
		String sqlGetDepartments = "SELECT * FROM department WHERE name ILIKE ?";
		SqlRowSet results = jdbcTemplate.queryForRowSet(sqlGetDepartments, "%" + nameSearch + "%");
		while(results.next()){
			departmentList.add(mapRowToDept(results));
		}
		return departmentList;
	}

	@Override
	public void updateDepartmentName(Long departmentId, String departmentName) {
		String sqlUpdateDepartment = "UPDATE department SET name = ? WHERE department_id = ?";
		jdbcTemplate.update(sqlUpdateDepartment, departmentName, departmentId);
	}

	@Override
	public Department createDepartment(String departmentName) {
		Department department = new Department();
		department.setName(departmentName);
		String sqlCreateDepartment = "INSERT INTO department (name) VALUES (?) RETURNING department_id";
		department.setId(jdbcTemplate.queryForObject(sqlCreateDepartment,Long.class, departmentName));
		return department;
		//Need to fix unique name constraint
	}

	@Override
	public Department getDepartmentById(Long id) {
		Department department = new Department();
		department.setId(id);
		String sqlGetDepartmentById = "SELECT name FROM department WHERE department_id = ?";
		department.setName(jdbcTemplate.queryForObject(sqlGetDepartmentById,String.class, id));
		return department;
	}
	
	private Department mapRowToDept(SqlRowSet results) {
		Department theDept = new Department();
		theDept.setId(results.getLong("department_id"));
		theDept.setName(results.getString("name"));
		return theDept;
	}

}
