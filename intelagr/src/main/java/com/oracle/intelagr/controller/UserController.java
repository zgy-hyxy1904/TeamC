package com.oracle.intelagr.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.oracle.intelagr.common.BaseModel;
import com.oracle.intelagr.common.CommonUtil;
import com.oracle.intelagr.common.JsonResult;
import com.oracle.intelagr.common.MD5Util;
import com.oracle.intelagr.common.PageModel;
import com.oracle.intelagr.entity.Role;
import com.oracle.intelagr.entity.User;
import com.oracle.intelagr.entity.UserRole;
import com.oracle.intelagr.service.IRoleService;
import com.oracle.intelagr.service.IUserService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private IUserService userService;
	@Autowired
	private IRoleService roleService;
	@RequestMapping("/login")
	@ResponseBody
	public JsonResult login(@RequestBody User user,HttpServletRequest request) {
		JsonResult result = null;
		List<User> list = userService.login(user);
		if(list.size()>0) {
			result = new JsonResult(true,"登录成功");
			HttpSession session = request.getSession();
			session.setAttribute("user", list.get(0));
		}else {
			result = new JsonResult(false,"用户名密码不正确");
		}
		return result;
		
	} 
	@RequestMapping("/main")
	public String mian(HttpServletRequest request,Map map) {
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
		List<Map> funList = userService.getFunction(user.getUserID());
		map.put("funList", funList);
		return "/main";
	}
	@RequestMapping("/list")
	public String list(User user,@RequestParam(defaultValue="1")int page,Model model) {
		//创建pageModel对象
		PageModel pageModel = new PageModel();
		//将数据放到map中
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("UserID", user.getUserID());
		map.put("userName", user.getUserName());
		map.put("userType", user.getUserType());
		map.put("index", (page-1)*pageModel.getPageSize());
		map.put("pageSize", pageModel.getPageSize());
		//将数据放到pageModel对象中
		pageModel.setData(map);
		pageModel.setPage(page);
		//调用service层方法
		userService.queryForPage(pageModel);
		model.addAttribute("pageModel",pageModel);
		return "user/userList";
	}
	@RequestMapping("/add")
	public String add(Model model) {
		List<Role> roleList = roleService.selectAll();
		JSONArray array = new JSONArray();
		for(Role role:roleList) {
			JSONObject obj = new JSONObject();
			obj.put("roleCode", role.getRoleCode());
			obj.put("roleName", role.getRoleName());
			array.add(obj);
		}
		model.addAttribute("roleList", array.toString());
		return "user/addUser";
	}
	@RequestMapping("/save")
	@ResponseBody
	public JsonResult save(@RequestBody User user,HttpServletRequest request) {
		BaseModel basemodel = CommonUtil.getBaseModel(request);
		user.setCreateUserId(basemodel.getCreateUserId());
		user.setCreateDate(basemodel.getCreateDate());
		user.setUpdateDate(basemodel.getUpdateDate());
		user.setUpdateUserId(basemodel.getUpdateUserId());
		user.setPassword(MD5Util.getMD5Code(user.getPassword()));
		String[] roleCodes = user.getRole().split(",");
		List<UserRole> list = new ArrayList<UserRole>();
		for(String roleCode:roleCodes) {
			UserRole userRole = new UserRole();
			userRole.setUserID(user.getUserID());
			userRole.setRoleCode(roleCode);
			BaseModel basemodel1 = CommonUtil.getBaseModel(request);
			userRole.setCreateUserId(basemodel1.getCreateUserId());
			userRole.setCreateDate(basemodel1.getCreateDate());
			userRole.setUpdateDate(basemodel1.getUpdateDate());
			userRole.setUpdateUserId(basemodel1.getUpdateUserId());
			list.add(userRole);
		}
		int result = userService.save(user,list);
		JsonResult jsonResult = null;
		if(result>0) {
			jsonResult = new JsonResult(true);
		}else {
			jsonResult = new JsonResult(false,"保存失败！！！");
		}
		return jsonResult;
		
	}
}
