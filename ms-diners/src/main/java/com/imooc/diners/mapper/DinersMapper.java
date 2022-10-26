package com.imooc.diners.mapper;

import com.imooc.commons.model.dto.DinersDTO;
import com.imooc.commons.model.pojo.Diners;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 食客Mapper
 */
public interface DinersMapper {
    /**
     * 根据手机号查询食客信息
     * @param phone
     * @return
     */
    @Select("select id, username, phone, email, is_valid "+
    " from t_diners where phone = #{phone}")
    Diners selectByPhone(@Param("phone") String phone);

    /**
     * 根据用户名查询食客信息
     * @param username
     * @return
     */
    @Select("select id, username, phone, email, is_valid "+
            " from t_diners where phone = #{username}")
    Diners selectByUsername(@Param("username") String username);

    /**
     * 插入食客信息
     * @param dinersDTO
     * @return
     */
    @Insert("insert into " +
            " t_diners (username, password, phone, roles, is_valid, create_date, update_date) " +
            " values (#{username}, #{password}, #{phone}, \"ROLE_USER\", 1, now(), now())")
    int save(DinersDTO dinersDTO); // 因为需要保存的信息只有 #{username}, #{password}, #{phone} 所以使用一个新的dto类来简化


}
