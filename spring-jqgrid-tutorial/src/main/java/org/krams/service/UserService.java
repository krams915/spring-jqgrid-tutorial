package org.krams.service;

import org.krams.domain.User;
import org.krams.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class UserService {

	@Autowired
	private UserRepository repository;
	
	public Boolean create(User user) {
		user.getRole().setUser(user);
		User saved = repository.save(user);
		if (saved == null) 
			return false;
		
		return true;
	}
	
	public Boolean update(User user) {
		User existingUser = repository.findByUsername(user.getUsername());
		if (existingUser == null) 
			return false;
		
		// Only firstName, lastName, and role fields are updatable
		existingUser.setFirstName(user.getFirstName());
		existingUser.setLastName(user.getLastName());
		existingUser.getRole().setRole(user.getRole().getRole());
		
		User saved = repository.save(existingUser);
		if (saved == null) 
			return false;
		
		return true;
	}
	
	public Boolean delete(User user) {
		User existingUser = repository.findByUsername(user.getUsername());
		if (existingUser == null) 
			return false;
		
		repository.delete(existingUser);
		User deletedUser = repository.findByUsername(user.getUsername());
		if (deletedUser != null) 
			return false;
		
		return true;
	}
}
