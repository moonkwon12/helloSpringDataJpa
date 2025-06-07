package kr.ac.hansung.cse.hellospringdatajpa.service;

import kr.ac.hansung.cse.hellospringdatajpa.dto.UserRegistrationDto;
import kr.ac.hansung.cse.hellospringdatajpa.entity.MyRole;
import kr.ac.hansung.cse.hellospringdatajpa.entity.MyUser;

import java.util.List;

public interface UserService {

    MyUser registerUser(UserRegistrationDto registrationDto);

    List<MyUser> getAllUsers();

    MyUser findByEmail(String email);

    boolean existsByEmail(String email);


}