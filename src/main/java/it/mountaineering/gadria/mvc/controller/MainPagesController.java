package it.mountaineering.gadria.mvc.controller;

import java.io.File;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class MainPagesController {

	@RequestMapping(value = "/resetEnvironmentPage", method = RequestMethod.GET)
	public String resetEnvironmentPageGet(@ModelAttribute("rdfUploadFileModel") File rdfUploadFileModel, HttpSession session, ModelMap model) {
		//sessionSetup(session);
		
		return "resetEnvironmentPage";
	}


}
