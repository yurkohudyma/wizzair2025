package ua.hudyma.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import ua.hudyma.domain.User;
import ua.hudyma.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Log4j2
public class UserService {
    private final UserRepository userRepository;


    public User updateUser (String userId, User newUser){
        var user = userRepository.findByUserId(userId).orElseThrow();
        mergeValuesIfNotNull(user, newUser);
        userRepository.save(user);
        return user;
    }

    private void mergeValuesIfNotNull(User user, User newUser) {
        if (!newUser.getStatus().name().isEmpty()){
            user.setStatus(newUser.getStatus());
        }
        if (!newUser.getProfile().getPhoneNumber().isEmpty()){
            var profile = user.getProfile();
            profile.setPhoneNumber(newUser.getProfile().getPhoneNumber());
            user.setProfile(profile);
        }
    }
}
