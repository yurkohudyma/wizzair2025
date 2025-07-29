package ua.hudyma.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.Contract;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.hudyma.domain.Address;
import ua.hudyma.domain.User;
import ua.hudyma.dto.PaxResponseDto;
import ua.hudyma.dto.UserDto;
import ua.hudyma.repository.BookingRepository;
import ua.hudyma.repository.UserRepository;

import static ua.hudyma.util.IdGenerator.generateId;

@Service
@RequiredArgsConstructor
@Log4j2
public class UserService {
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    public User addUser(User user) {
        user.setUserId(generateId(8));
        var wdc = user.getAccount();
        if (wdc != null) {
            wdc.setUser(user);
        }
        userRepository.save(user);
        return user;
    }

    @Transactional(readOnly = true)
    public PaxResponseDto getPax(String confirmCode) {
        var booking = bookingRepository
                .findByConfirmationCode(confirmCode)
                .orElseThrow();
        var mainUser = booking.getMainUser();
        var userList = booking.getUserList();
        var list = userList.stream()
                .map(
                user -> new UserDto(user.getId()))
                .toList();
        return new PaxResponseDto(
                new UserDto(mainUser.getId()),
                list);
    }


    public User updateUser(String userId, User newUser) {
        var user = userRepository.findByUserId(userId).orElseThrow();
        mergeValuesIfNotNull(user, newUser);
        userRepository.save(user);
        return user;
    }

    //@Transactional
    public User updateUser(String userId, Address address) {
        var user = userRepository.findByUserId(userId).orElseThrow();
        applyAddress(user, address);
        userRepository.save(user);
        return user;
    }

    @Contract("_, _ -> param1")
    private void applyAddress(User user, Address address) {
        //var profile = user.getProfile();
        var addressList = user.getAddressList();
        addressList.add(address);
        /*profile.setAddressList(addressList);
        user.setProfile(profile);*/
    }

    private void mergeValuesIfNotNull(User user, User newUser) {
        var profile = user.getProfile();
        if (!newUser.getStatus().name().isEmpty()) {
            user.setStatus(newUser.getStatus());
        }
        if (!newUser.getProfile().getPhoneNumber().isEmpty()) {
            profile.setPhoneNumber(newUser.getProfile().getPhoneNumber());
            user.setProfile(profile);
        }
    }
}
