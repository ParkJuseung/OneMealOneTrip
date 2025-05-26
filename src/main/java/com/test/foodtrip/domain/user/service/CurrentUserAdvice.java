package com.test.foodtrip.domain.user.service;

import com.test.foodtrip.domain.user.entity.User;
import com.test.foodtrip.domain.user.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class CurrentUserAdvice {

    private final UserRepository userRepository;

    public CurrentUserAdvice(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 모든 컨트롤러에 currentUser 라는 이름으로
     * 세션의 user_id에 해당하는 User 엔티티를 바인딩합니다.
     */
    @ModelAttribute("currentUser")
    public User getCurrentUser(HttpSession session) {
        Object attr = session.getAttribute("user_id");
        if (!(attr instanceof Long)) {
            return null;
        }
        Long userId = (Long) attr;
        return userRepository.findById(userId).orElse(null);
    }
}
