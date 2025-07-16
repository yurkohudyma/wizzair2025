package ua.hudyma.service;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.grpc.server.service.GrpcService;
import ua.hudyma.domain.model.Profile;
import ua.hudyma.domain.model.User;
import ua.hudyma.domain.repository.UserRepository;
import ua.hudyma.grpc.user.*;

import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Log4j2
@GrpcService
@RequiredArgsConstructor
public class UserService extends UserServiceGrpc.UserServiceImplBase {

    private final UserRepository userRepository;

    @Override
    public void createUser(CreateUserRequest request, StreamObserver<UserResponse> responseObserver) {
        var user = new User();
        var profile = new Profile();
        profile.setName(request.getName());
        profile.setSurname(request.getSurname());
        profile.setBirthday(parseDate(request.getBirthday()));
        profile.setEmail(request.getEmail());
        profile.setPhoneNumber(request.getPhoneNumber());
        user.setUserId(initUserId());
        profile.setRegisteredOn(new Date());
        user.setProfile(profile);

        userRepository.save(user);

        UserResponse response = UserResponse.newBuilder()
                .setUserId(user.getUserId())
                .setName(profile.getName())
                .setSurname(profile.getSurname())
                .setBirthday(formatDate(profile.getBirthday()))
                .setRegisteredOn(formatDate(profile.getRegisteredOn()))
                .setEmail(profile.getEmail())
                .setPhoneNumber(profile.getPhoneNumber())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteUser(DeleteUserRequest request, StreamObserver<DeleteUserResponse> responseObserver) {
        String userId = request.getUserId();
        boolean success = false;
        String message;

        try {
            var user = userRepository.findByUserId(userId);
            if (user.isPresent()) {
                userRepository.delete(user.get());
                success = true;
            }
            message = success ? "User deleted successfully" : "User not found";
        } catch (Exception e) {
            message = "Error deleting user: " + e.getMessage();
        }
        DeleteUserResponse response = DeleteUserResponse.newBuilder()
                .setSuccess(success)
                .setMessage(message)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void updateUser(UpdateUserRequest request, StreamObserver<UserResponse> responseObserver) {
        String userId = request.getUserId();
        var userOpt = userRepository.findByUserId(userId);
        if (userOpt.isEmpty()) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("User with ID " + userId + " not found.")
                            .asRuntimeException()
            );
            return;
        }
        var user = userOpt.get();
        var profile = user.getProfile();
        if (!request.getName().isEmpty()) {
            profile.setName(request.getName());
        }
        if (!request.getSurname().isEmpty()) {
            profile.setSurname(request.getSurname());
        }
        if (!request.getBirthday().isEmpty()) {
            try {
                Date birthday = new SimpleDateFormat("dd-MM-yyyy").parse(request.getBirthday());
                profile.setBirthday(birthday);
            } catch (ParseException e) {
                responseObserver.onError(
                        Status.INVALID_ARGUMENT
                                .withDescription("Invalid birthday format. Use dd-MM-yyyy.")
                                .asRuntimeException()
                );
                return;
            }
        }
        if (!request.getEmail().isEmpty()) {
            profile.setEmail(request.getEmail());
        }
        if (!request.getPhoneNumber().isEmpty()) {
            profile.setPhoneNumber(request.getPhoneNumber());
        }
        if (!request.getPassword().isEmpty()) {
            profile.setPassword(request.getPassword());
        }
        userRepository.save(user);
        UserResponse response = UserResponse.newBuilder()
                .setUserId(user.getUserId())
                .setName(profile.getName())
                .setSurname(profile.getSurname())
                .setBirthday(new SimpleDateFormat("dd-MM-yyyy").format(profile.getBirthday()))
                .setRegisteredOn(new SimpleDateFormat("dd-MM-yyyy").format(profile.getRegisteredOn()))
                .setEmail(profile.getEmail())
                .setPhoneNumber(profile.getPhoneNumber())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getUser(GetUserRequest request, StreamObserver<UserResponse> responseObserver) {
        String userId = request.getUserId();

        var userOpt = userRepository.findByUserId(userId);
        if (userOpt.isEmpty()) {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("User with ID " + userId + " not found.")
                            .asRuntimeException()
            );
            return;
        }

        var user = userOpt.get();
        var profile = user.getProfile();

        UserResponse response = UserResponse.newBuilder()
                .setUserId(user.getUserId())
                .setName(profile.getName())
                .setSurname(profile.getSurname())
                .setBirthday(formatDate(profile.getBirthday()))
                .setRegisteredOn(formatDate(profile.getRegisteredOn()))
                .setEmail(profile.getEmail())
                .setPhoneNumber(profile.getPhoneNumber())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }







    private Date parseDate(String date) {
        try {
            return new SimpleDateFormat("dd-MM-yyyy").parse(date);
        } catch (ParseException e) {
            throw Status.INVALID_ARGUMENT
                    .withDescription("Invalid birthday format. Expected dd-MM-yyyy")
                    .asRuntimeException();
        }
    }

    private String formatDate(Date date) {
        return new SimpleDateFormat("dd-MM-yyyy").format(date);
    }

    private String initUserId() {
        return NanoIdUtils.randomNanoId(new SecureRandom(), NanoIdUtils.DEFAULT_ALPHABET, 8);
    }
}




