package kr.ac.hansung.cse.hellospringdatajpa.service;

import kr.ac.hansung.cse.hellospringdatajpa.dto.UserRegistrationDto;
import kr.ac.hansung.cse.hellospringdatajpa.entity.MyRole;
import kr.ac.hansung.cse.hellospringdatajpa.entity.MyUser;
import kr.ac.hansung.cse.hellospringdatajpa.repo.RoleRepository;
import kr.ac.hansung.cse.hellospringdatajpa.repo.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    // 관리자 이메일 목록 - 필요시 확장 가능
    private static final List<String> ADMIN_EMAILS = Arrays.asList(
            "admin@hansung.ac.kr"
            // 추가 관리자 이메일이 필요하면 여기에 추가
            // "supervisor@hansung.ac.kr",
            // "manager@hansung.ac.kr"
    );

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public MyUser registerUser(UserRegistrationDto registrationDto) {
        logger.info("사용자 등록 시작: {}", registrationDto.getEmail());

        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            logger.warn("이미 등록된 이메일: {}", registrationDto.getEmail());
            throw new RuntimeException("이미 등록된 이메일입니다.");
        }

        MyUser user = new MyUser();
        user.setEmail(registrationDto.getEmail());
        user.setName(registrationDto.getName());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));

        List<MyRole> userRoles = new ArrayList<>();

        // 기본 USER 역할 추가
        MyRole userRole = findOrCreateRole("ROLE_USER");
        userRoles.add(userRole);
        logger.info("USER 역할 추가: {}", registrationDto.getEmail());

        // 특정 이메일 주소인 경우 ADMIN 역할 추가
        if (ADMIN_EMAILS.contains(registrationDto.getEmail())) {
            MyRole adminRole = findOrCreateRole("ROLE_ADMIN");
            userRoles.add(adminRole);
            logger.info("ADMIN 역할 추가: {}", registrationDto.getEmail());
        }

        user.setRoles(userRoles);
        MyUser savedUser = userRepository.save(user);

        logger.info("사용자 등록 완료: {} (ID: {})", savedUser.getEmail(), savedUser.getId());
        return savedUser;
    }

    private MyRole findOrCreateRole(String rolename) {
        return roleRepository.findByRolename(rolename)
                .orElseGet(() -> {
                    logger.info("새로운 역할 생성: {}", rolename);
                    MyRole newRole = new MyRole(rolename);
                    return roleRepository.save(newRole);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<MyUser> getAllUsers() {
        try {
            logger.info("모든 사용자 조회 시작");
            List<MyUser> users = userRepository.findAll();

            // Lazy Loading 문제 해결을 위해 roles를 미리 로드
            for (MyUser user : users) {
                if (user.getRoles() != null) {
                    user.getRoles().size(); // Lazy Loading 강제 실행
                    logger.debug("사용자 {} 의 역할 로드 완료", user.getEmail());
                }
            }

            logger.info("모든 사용자 조회 완료: {} 명", users.size());
            return users;
        } catch (Exception e) {
            logger.error("사용자 조회 중 오류 발생: ", e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public MyUser findByEmail(String email) {
        logger.debug("이메일로 사용자 조회: {}", email);
        return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    public boolean existsByEmail(String email) {
        boolean exists = userRepository.existsByEmail(email);
        logger.debug("이메일 중복 확인 - {}: {}", email, exists);
        return exists;
    }
}