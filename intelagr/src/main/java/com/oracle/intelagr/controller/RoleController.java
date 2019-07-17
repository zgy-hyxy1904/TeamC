package com.oracle.intelagr.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.oracle.intelagr.common.JsonResult;
import com.oracle.intelagr.common.PageModel;
import com.oracle.intelagr.common.TreeModel;
import com.oracle.intelagr.entity.Function;
import com.oracle.intelagr.entity.Role;
import com.oracle.intelagr.entity.RoleFunction;
import com.oracle.intelagr.entity.User;
import com.oracle.intelagr.service.IFunctionService;
import com.oracle.intelagr.service.IRoleFunctionService;
import com.oracle.intelagr.service.IRoleService;

import net.sf.json.JSONArray;

@Controller
@RequestMapping("/role")
public class RoleController {
	@Autowired
	private IRoleService roleService;
	@Autowired
	private IRoleFunctionService roleFunctionService;
	@Autowired
	private IFunctionService functionService;
	@RequestMapping("/list")
	public String list(Map map,@RequestParam(defaultValue = "1")int page,Role role) {
		PageModel pageModel = new PageModel();
		pageModel.setData(role);
		pageModel.setPage(page);
		roleService.queryForPage(pageModel);
		map.put("pageModel", pageModel);
		return "role/roleList";	
	}
	@RequestMapping("/roleAuth")
	public String roleAuth(int id,Model model) {
		//1.通过id找到role对象（）
		Role role = roleService.queryById(id);
		//2.角色拥有的权限
		Map queryMap = new HashMap();
		queryMap.put("roleCode", role.getRoleCode());
		List<RoleFunction> hasFunctionList = roleFunctionService.query(queryMap);
		//3.所有权限
		List<Function> functionList = functionService.selectAll();
		//4.所有权限组装成要求的JSON格式
		List<Map> listMap = new ArrayList<Map>();
		Map map = new HashMap();
		for(Function function:functionList) {
			if(function.getModuleName()!=null) {
				if(map.get(function.getModuleName())==null) {
					HashMap m = new HashMap();
					map.put(function.getModuleName(),m);
					m.put("parent", function);
					List<Function> childList = new ArrayList<Function> ();
					childList.add(function);
					m.put("child", childList);
					listMap.add(m);
				}else {
					Map m = (Map)map.get(function.getModuleName());
					List<Function> childList = (List<Function>)m.get("child");
					childList.add(function);
				}					
			}									
		}
		JSONArray array = new JSONArray();
		for(Map m:listMap) {
			TreeModel parent = new TreeModel();
			Function parentFunction = (Function)m.get("parent");
			parent.setId(String.valueOf(parentFunction.getModuleCode()));
			parent.setText(parentFunction.getModuleName());
			List<TreeModel> childList = new ArrayList<TreeModel>();
			for(Function f:(List<Function>)m.get("child")) {
				TreeModel child = new TreeModel();
				child.setId(String.valueOf(f.getFunctionCode()));
				child.setText(f.getModuleName());
				for(RoleFunction rolefun:hasFunctionList) {
					if(f.getFunctionCode().equals(rolefun.getFunctionCode())) {
						child.setChecked("true");
					}
				}
				childList.add(child);
			}
			parent.setChildren(childList);
			array.add(parent);
		}
		model.addAttribute("jsonData", array.toString());
		model.addAttribute("role", role);
		return "role/roleAuth";	
	}
	@RequestMapping("/saveRoleAuth")
	@ResponseBody
	public JsonResult saveRoleAuth(@RequestBody Role role,HttpServletRequest request) {
		User user = (User)request.getSession().getAttribute("user");
		String[] funIds = new String[role.getFunctions().size()];
		int i = 0;
		for(Function fun:role.getFunctions()) {
			funIds[i++] = fun.getFunctionCode();
		}
		roleService.saveRoleAuth(role.getRoleCode(), funIds, user);
		return new JsonResult(true);
	}
}
