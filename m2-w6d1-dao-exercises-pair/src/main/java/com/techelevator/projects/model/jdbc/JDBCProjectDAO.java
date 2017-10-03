package com.techelevator.projects.model.jdbc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.techelevator.projects.model.Employee;
import com.techelevator.projects.model.Project;
import com.techelevator.projects.model.ProjectDAO;

public class JDBCProjectDAO implements ProjectDAO {

	private JdbcTemplate jdbcTemplate;

	public JDBCProjectDAO(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	@Override
	public List<Project> getAllActiveProjects() {
		List<Project> activeProjectList = new ArrayList<>();
		String sqlGetEmployeeById = "SELECT * FROM project WHERE from_date <= NOW() AND (to_date >= NOW() OR to_date IS NULL)";
		SqlRowSet results = (jdbcTemplate.queryForRowSet(sqlGetEmployeeById));
		while(results.next()){
			activeProjectList.add(mapRowToProject(results));
		}
		return activeProjectList;
	}

	@Override
	public void removeEmployeeFromProject(Long projectId, Long employeeId) {
		String sqlRemoveEmployeeFromProject = "DELETE FROM project_employee WHERE employee_id = ? AND project_id = ?";
		jdbcTemplate.update(sqlRemoveEmployeeFromProject, employeeId, projectId);
	}

	@Override
	public void addEmployeeToProject(Long projectId, Long employeeId) {
		String sqlAddEmployeeFromProject = "INSERT INTO project_employee (employee_id, project_id) VALUES (?, ?)";
		jdbcTemplate.update(sqlAddEmployeeFromProject, employeeId, projectId);
	}
	public Project createProject(String name, LocalDate fromDate, LocalDate toDate) {
		Project project = new Project();
		project.setName(name);
		project.setStartDate(fromDate);
		project.setEndDate(toDate);
		String sqlCreateProject = "INSERT INTO project (name, from_date, to_date) VALUES (?,?,?) RETURNING project_id";
		project.setId(jdbcTemplate.queryForObject(sqlCreateProject,Long.class, name, fromDate, toDate));
		return project;
	}
	private Project mapRowToProject(SqlRowSet results) {
		Project theProj = new Project();
		theProj.setId(results.getLong("project_id"));
		theProj.setName(results.getString("name"));
		if(results.getDate("from_date") != null){
		theProj.setStartDate(results.getDate("from_date").toLocalDate());
		}
		if(results.getDate("to_date") != null){
		theProj.setEndDate(results.getDate("to_date").toLocalDate());
		}
		return theProj;
	}

}
