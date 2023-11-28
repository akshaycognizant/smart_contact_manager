package com.smart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController {
	
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@RequestMapping(value="/",method=RequestMethod.GET)
	public String home(Model m) {
		m.addAttribute("title", "home - smart contact manager");
		return "home";
	}
	
	@RequestMapping(value="/about",method=RequestMethod.GET)
	public String about(Model m) {
		m.addAttribute("title", "about - smart contact manager");
		return "about";
	}

	@RequestMapping(value="/signup",method=RequestMethod.GET)
	public String signup(Model m) {
		m.addAttribute("title", "register - smart contact manager");
		m.addAttribute("user", new User());
		return "signup";
	}
	
	@RequestMapping(value="/do_register",method=RequestMethod.POST)
	public String register(@Valid @ModelAttribute("user") User user,BindingResult bresult,@RequestParam(value="aggrement",defaultValue="false") boolean aggrement,Model model,HttpSession session) {
		try {
			if(!aggrement) {
				System.out.println("you have to agree terms and conditions");
				throw new Exception("you have to agree terms and conditions");
			}
			if(bresult.hasErrors()) {
				System.out.println("Error "+ bresult.toString());
				model.addAttribute("user", user);
				return "signup";
			}
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			User result = this.userRepository.save(user);
			
			System.out.println(result);
			System.out.println(aggrement);
			System.out.println(user);
			model.addAttribute("user", new User());
			session.setAttribute("message", new Message("successfully registered." ,"alert-success"));
			return "signup";
			
		}catch(Exception e) {
			e.printStackTrace();
			model.addAttribute("user",user);
			session.setAttribute("message", new Message("something went wrong!! " + e.getMessage(),"alert-danger"));
			return "signup";
		}	
	}
	
	@RequestMapping(value="/signin",method=RequestMethod.GET)
	public String customLogin(Model m) {
		m.addAttribute("title", "login-page");
		return "login";
	}
	
}
