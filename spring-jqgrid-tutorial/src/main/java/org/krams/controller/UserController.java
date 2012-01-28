package org.krams.controller;

import java.util.List;

import org.krams.domain.Role;
import org.krams.domain.User;
import org.krams.repository.UserRepository;
import org.krams.response.JqgridResponse;
import org.krams.response.StatusResponse;
import org.krams.response.UserDto;
import org.krams.service.UserService;
import org.krams.util.JqgridFilter;
import org.krams.util.JqgridObjectMapper;
import org.krams.util.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/users")
public class UserController {
	
	@Autowired
	private UserRepository repository;
	
	@Autowired 
	private UserService service;
	
	@RequestMapping
	public String getUsersPage() {
		return "users";
	}
	
	@RequestMapping(value="/records", produces="application/json")
	public @ResponseBody JqgridResponse<UserDto> records(
    		@RequestParam("_search") Boolean search,
    		@RequestParam(value="filters", required=false) String filters,
    		@RequestParam(value="page", required=false) Integer page,
    		@RequestParam(value="rows", required=false) Integer rows,
    		@RequestParam(value="sidx", required=false) String sidx,
    		@RequestParam(value="sord", required=false) String sord) {

		Pageable pageRequest = new PageRequest(page-1, rows);
		
		if (search == true) {
			return getFilteredRecords(filters, pageRequest);
			
		} 
			
		Page<User> users = repository.findAll(pageRequest);
		List<UserDto> userDtos = UserMapper.map(users);
		
		JqgridResponse<UserDto> response = new JqgridResponse<UserDto>();
		response.setRows(userDtos);
		response.setRecords(Long.valueOf(users.getTotalElements()).toString());
		response.setTotal(Integer.valueOf(users.getTotalPages()).toString());
		response.setPage(Integer.valueOf(users.getNumber()+1).toString());
		
		return response;
	}
	
	/**
	 * Helper method for returning filtered records
	 */
	public JqgridResponse<UserDto> getFilteredRecords(String filters, Pageable pageRequest) {
		String qUsername = null;
		String qFirstName = null;
		String qLastName = null;
		Integer qRole = null;
		
		JqgridFilter jqgridFilter = JqgridObjectMapper.map(filters);
		for (JqgridFilter.Rule rule: jqgridFilter.getRules()) {
			if (rule.getField().equals("username"))
				qUsername = rule.getData();
			else if (rule.getField().equals("firstName"))
				qFirstName = rule.getData();
			else if (rule.getField().equals("lastName"))
				qLastName = rule.getData();
			else if (rule.getField().equals("role"))
				qRole = Integer.valueOf(rule.getData());
		}
		
		Page<User> users = null;
		if (qUsername != null) 
			users = repository.findByUsernameLike("%"+qUsername+"%", pageRequest);
		if (qFirstName != null && qLastName != null) 
			users = repository.findByFirstNameLikeAndLastNameLike("%"+qFirstName+"%", "%"+qLastName+"%", pageRequest);
		if (qFirstName != null) 
			users = repository.findByFirstNameLike("%"+qFirstName+"%", pageRequest);
		if (qLastName != null) 
			users = repository.findByLastNameLike("%"+qLastName+"%", pageRequest);
		if (qRole != null) 
			users = repository.findByRole(qRole, pageRequest);
		
		List<UserDto> userDtos = UserMapper.map(users);
		JqgridResponse<UserDto> response = new JqgridResponse<UserDto>();
		response.setRows(userDtos);
		response.setRecords(Long.valueOf(users.getTotalElements()).toString());
		response.setTotal(Integer.valueOf(users.getTotalPages()).toString());
		response.setPage(Integer.valueOf(users.getNumber()+1).toString());
		return response;
	}
	
	@RequestMapping(value="/get", produces="application/json")
	public @ResponseBody UserDto get(@RequestBody UserDto user) {
		return UserMapper.map(repository.findByUsername(user.getUsername()));
	}

	@RequestMapping(value="/create", produces="application/json", method=RequestMethod.POST)
	public @ResponseBody StatusResponse create(
			@RequestParam String username,
			@RequestParam String password,
			@RequestParam String firstName,
			@RequestParam String lastName,
			@RequestParam Integer role) {
		
		User newUser = new User(username, password, firstName, lastName, new Role(role));
		Boolean result = service.create(newUser);
		return new StatusResponse(result);
	}
	
	@RequestMapping(value="/update", produces="application/json", method=RequestMethod.POST)
	public @ResponseBody StatusResponse update(
			@RequestParam String username,
			@RequestParam String firstName,
			@RequestParam String lastName,
			@RequestParam Integer role) {
		
		User existingUser = new User(username, firstName, lastName, new Role(role));
		Boolean result = service.update(existingUser);
		return new StatusResponse(result);
	}
	
	@RequestMapping(value="/delete", produces="application/json", method=RequestMethod.POST)
	public @ResponseBody StatusResponse delete(
			@RequestParam String username) {

		User existingUser = new User(username);
		Boolean result = service.delete(existingUser);
		return new StatusResponse(result);
	}
}
