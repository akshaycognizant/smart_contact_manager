package com.smart.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import com.smart.entities.Contact;
import com.smart.entities.User;

import jakarta.transaction.Transactional;


public interface ContactRepository extends JpaRepository<Contact,Integer>{
  //pagination
	@Query("from Contact c where c.user.id=:userId")
  public Page<Contact> findContactsByUser(@Param("userId")int userId,Pageable pageable);
	
		@Query("from Contact c where c.user.id=:userId")
	  public List<Contact> findContactsByUser(@Param("userId")int userId);
		
		@Modifying
	    @Transactional
	    @Query("delete from Contact c where c.cid =:id")
	    public void deleteContactById(@Param("id") int id);
		
		//search
		public List<Contact> findByNameContainingAndUser(String keyWord,User user);
}
