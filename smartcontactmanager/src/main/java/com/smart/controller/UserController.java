package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.data.domain.Page;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@ModelAttribute
	public void addCommonData(Model model,Principal principal) {
		String name = principal.getName();
		//System.out.println("username-> "+name);
		
		User user = userRepository.getUserByUserName(name);
		//System.out.println("user : " + user);
		
		model.addAttribute("user", user);
	}
	
	@RequestMapping("/index")
	public String dashboard(Model model) {
		model.addAttribute("title","dashboard");
		return "normal/user_dashboard";
	}
	
	@RequestMapping(value="/add-contact",method=RequestMethod.GET)
	public String openaddContact(Model model) {
		model.addAttribute("title","add-contact");
		model.addAttribute("contact",new Contact());
		return "normal/add_contact";
	}
	
	@RequestMapping(value="/process-contact",method=RequestMethod.POST)
	public String processContact(@ModelAttribute Contact contact,@RequestParam ("profileImage") MultipartFile file ,Principal principal,HttpSession session) {

		try {
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);
			
			//processing and uploading file
			if(file.isEmpty()) {
				System.out.println("file is empty");
				contact.setImage("contact.png");
			}
			else {
				contact.setImage(file.getOriginalFilename());
				
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("image is uploaded");
				
			}
			//success message
			session.setAttribute("message",new Message("contact is added..","success"));
			contact.setUser(user);
			user.getContacts().add(contact);
			this.userRepository.save(user);
			
			System.out.println("data "+contact);
			System.out.println("Added to database");
		}
		catch(Exception e) {
			System.out.println("Error "+ e.getMessage());
			e.printStackTrace();
			//error message
			session.setAttribute("message",new Message("something went wrong"+e.getMessage(),"danger"));
		}
		return "normal/add_contact";
	}

	@RequestMapping(value="/view-contact/{page}",method=RequestMethod.GET)
    public String viewContact(@PathVariable("page") Integer page,Model model,Principal principal) {
    	model.addAttribute("title","view-contact");
    	
    	String name = principal.getName();
    	User user = this.userRepository.getUserByUserName(name);
    	Pageable pageable = PageRequest.of(page, 3);
    	Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(),pageable);
    	List<Contact> conList = this.contactRepository.findContactsByUser(user.getId());
    	model.addAttribute("contacts",contacts);
    	model.addAttribute("size",contacts.getSize());
    	System.out.println("size -> "+ conList.size());
    	model.addAttribute("currentPage",page);
    	model.addAttribute("totalPages",contacts.getTotalPages());
    	
		return "normal/view_contact";
    }
	
	@RequestMapping(value="/contact-detail/{cid}",method=RequestMethod.GET)
	public String showParticularContactDetail(@PathVariable("cid") Integer cid,Model model,Principal principal) {
		
		Contact contact = this.contactRepository.findById(cid).get();
		
		String name = principal.getName();
    	User user = this.userRepository.getUserByUserName(name);
    	if(user.getId()== contact.getUser().getId()) {
    		model.addAttribute("contact",contact);
    		model.addAttribute("title",contact.getName());
    	}
		
		return "normal/contact_detail";
	}
	
	//delete contact handler
	@RequestMapping(value="/delete/{cid}",method=RequestMethod.GET)
	public String deleteContact(@PathVariable("cid") Integer cid,Principal principal,HttpSession session) {
		Contact contact = this.contactRepository.findById(cid).get();
		
		String name = principal.getName();
    	User user = this.userRepository.getUserByUserName(name);
    	
    	if(user.getId() == contact.getUser().getId()){
    		this.contactRepository.delete(contact);
    		session.setAttribute("message", new Message("Conatct Deleted successfully  !!", "success"));
    	}
    	contact.setUser(null);
		return "redirect:/user/view-contact/0";
	}
	
	@RequestMapping(value="/profile",method=RequestMethod.GET)
	public String yourProfile(Model model) {
		model.addAttribute("title","profile-view");
		return "normal/profile";
	}
	
	//open update form handler
	@RequestMapping(value="/update-contact/{cid}",method=RequestMethod.POST)
	public String updateContact(@PathVariable("cid") Integer cid,Model model) {
		model.addAttribute("title","update-contact");
		
		Contact contact = this.contactRepository.findById(cid).get();
		model.addAttribute("contact",contact);
		return "normal/update_form";
	}
	
	//update contact handler
	@RequestMapping(value="/process-update",method=RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact,@RequestParam ("profileImage") MultipartFile file,Model model,HttpSession session,Principal principal) {
		try {
			//old contact details
			Contact oldContact = this.contactRepository.findById(contact.getCid()).get();
			if(!file.isEmpty()) {
				//file work
				
				//rework
				//delete old photo
				File deleteFile = new ClassPathResource("static/img").getFile();
				File file1 = new File(deleteFile,oldContact.getImage());
				file1.delete();
				
				//update new photo
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
			}
			else {
				contact.setImage(oldContact.getImage());
			}
			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			session.setAttribute("message",new Message("your contact is updated","success"));
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("Contact name "+contact.getName());
		System.out.println("Contact id "+contact.getCid());
		return "redirect:/user/contact-detail/"+contact.getCid();
	}
	
	//open settings handler
	@RequestMapping(value="/settings",method=RequestMethod.GET)
	public String openSetting(Model model) {
		model.addAttribute("title","settings");
		return "normal/settings";
	}
	
	//change password handler
	@RequestMapping(value="/change-password",method=RequestMethod.POST)
	public String changePassword(@RequestParam("oldPassword") String oldPassword,@RequestParam("newPassword") String newPassword,Model model,Principal principal,HttpSession session) {
		User user = this.userRepository.getUserByUserName(principal.getName());
		
		if(this.passwordEncoder.matches(oldPassword, user.getPassword())) {
			user.setPassword(this.passwordEncoder.encode(newPassword));
			this.userRepository.save(user);
			session.setAttribute("message", new Message("password changed successfully!..","success"));
		}
		else {
			session.setAttribute("message", new Message("OOPS! entered wrong password","danger"));
			return "normal/settings";
		}
		
		model.addAttribute("title","settings");
		System.out.println("OLD PASS ->"+oldPassword);
		System.out.println("NEW PASS ->"+newPassword);
		return "normal/user_dashboard";
	}
	
}

