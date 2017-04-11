package spring.boot.admin.deploy.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import de.codecentric.boot.admin.web.AdminController;

/**
 * 
 * @author chun
 *
 */
@AdminController
@ResponseBody
@RequestMapping("/api/deploy")
public interface DeployController {}
