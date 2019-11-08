package ru.sberbank.syncserver2.service.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

/**
 * Created with IntelliJ IDEA.
 * User: SBT-Kolmakov-AV
 * Date: 04.08.13
 * Time: 23:19
 * To change this template use File | Settings | File Templates.
 */
public class UserDetailsLdapContextMapper implements UserDetailsContextMapper {

//    @Qualifier("jdbcTemplate")
//    @Autowired
//    private JdbcTemplate jdbcTemplate;

    @Override
    public UserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection<? extends GrantedAuthority> authorities) {
//        String mail = ctx.getStringAttribute("mail");
//        List<String> listPerms = jdbcTemplate.queryForList("select p.permission from interviewees u join user_group_permission p on u.seq_group = p.user_group where u.email = ? and u.deleted = 0", String.class, new Object[]{mail});
//        ArrayList<GrantedAuthority> newauthorities = new ArrayList<GrantedAuthority>();
//        for (String permission : listPerms) {
//            newauthorities.add(new SimpleGrantedAuthority(permission));
//        }
//
//        return new User(username, "", newauthorities);
    	return new User(username, "", authorities);
    }

    @Override
    public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
//        this.jdbcTemplate = jdbcTemplate;
    }

}
