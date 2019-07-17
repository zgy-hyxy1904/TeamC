package com.oracle.intelagr.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oracle.intelagr.common.MD5Util;
import com.oracle.intelagr.common.PageModel;
import com.oracle.intelagr.entity.Function;
import com.oracle.intelagr.entity.Role;
import com.oracle.intelagr.entity.User;
import com.oracle.intelagr.entity.UserRole;
import com.oracle.intelagr.mapper.UserMapper;
import com.oracle.intelagr.mapper.UserRoleMapper;
import com.oracle.intelagr.service.IUserService;

@Service
public class UserService implements IUserService{
	@Autowired
	private UserMapper userMapper;
	@Autowired
	private UserRoleMapper userRoleMapper;

	@Override
	public List<User> login(User user) {
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("userID", user.getUserID());
		map.put("password", MD5Util.getMD5Code(user.getPassword()));
		return userMapper.select(map);
	}

	@Override
	public List<Map> getFunction(String userID) {
		User user = userMapper.selectById(userID);
		List<Map> list = new ArrayList<Map>();
		Map map = new HashMap();
		for(Role role:user.getRoles()) {
			for(Function function:role.getFunctions()) {
				if(function.getModuleName()!=null) {
					if(map.get(function.getModuleName())==null) {
						HashMap m = new HashMap();
						map.put(function.getModuleName(),m);
						m.put("parent", function);
						List<Function> childList = new ArrayList<Function> ();
						childList.add(function);
						m.put("child", childList);
						list.add(m);
					}else {
						Map m = (Map)map.get(function.getModuleName());
						List<Function> childList = (List<Function>)m.get("child");
						childList.add(function);
					}					
				}									
			}
		}
		return list;
	}

	@Override
	public void queryForPage(PageModel pageModel) {
		List<User> list =  userMapper.select((Map<String,Object>)pageModel.getData());
		int totalCount = userMapper.count((Map<String,Object>)pageModel.getData());
		pageModel.setResult(list);
		pageModel.setTotalCount(totalCount);
		
	}

	@Override
	@Transactional
	public int save(User user,List<UserRole> list) {
		int result = 0;
		result = userMapper.insert(user);
		for(UserRole userRole:list) {
			userRoleMapper.insert(userRole);
		}
		return result;
		
	}

	@Override
	public User selectById(String userID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int update(User user) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void delete(String userID) {
		//É¾³ýuser±í
		User user = new User();
		user.setUserID(userID);
		user.setDeleteFlag("Y");
		userMapper.update(user);
		//É¾³ýuserRoleMap±í
		
		
	}

	@Override
	public void resetPwd(String userID, String password) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startUse(String userID) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void endUse(String userID) {
		// TODO Auto-generated method stub
		
	}
	
}
