package com.study.shortlink.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.study.shortlink.pojo.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {

}
