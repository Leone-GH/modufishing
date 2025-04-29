package com.fishtripplanner.mapper;

import com.fishtripplanner.dto.UserMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface macbook{
    List<UserMapper> selectAll();


}
