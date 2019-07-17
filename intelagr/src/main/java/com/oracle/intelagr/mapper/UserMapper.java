package com.oracle.intelagr.mapper;

import java.util.List;
import java.util.Map;

import com.oracle.intelagr.entity.User;

public interface UserMapper {
	public List<User> select(Map<String,Object> map);
	public User selectById(String userID);
	public int count(Map<String,Object> map);
	public int insert(User user);
	public int update(User user);
}
