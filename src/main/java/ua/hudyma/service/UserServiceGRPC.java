package ua.hudyma.service;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.grpc.server.service.GrpcService;
import ua.hudyma.domain.Profile;
import ua.hudyma.domain.User;
import ua.hudyma.grpc.user.*;
import ua.hudyma.repository.UserRepository;
import ua.hudyma.validator.PhoneNumberValidator;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

import static ua.hudyma.util.IdGenerator.generateId;

@Log4j2
@GrpcService
@RequiredArgsConstructor
public class UserServiceGRPC extends UserServiceGrpc.UserServiceImplBase {

    public static final String DD_MM_YYYY = "dd-MM-yyyy";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private final UserRepository userRepository;
    private final PhoneNumberValidator validator;

    @Override
    public void createUser(CreateUserRequest request, StreamObserver<UserResponse> responseObserver) {
        var user = new User();
        var profile = new Profile();
        profile.setName(request.getName());
        profile.setSurname(request.getSurname());
        profile.setBirthday(parseDate(request.getBirthday()));
        profile.setEmail(request.getEmail());
        profile.setPhoneNumber(request.getPhoneNumber());
        user.setUserId(generateId(8));
        profile.setRegisteredOn(new Date());
        user.setProfile(profile);
        user.setStatus(User.UserStatus.ACTIVE);

        userRepository.save(user);

        UserResponse response = UserResponse.newBuilder()
                .setUserId(user.getUserId())
                .setName(profile.getName())
                .setSurname(profile.getSurname())
                .setBirthday(stringifyDate(profile.getBirthday()))
                .setRegisteredOn(profile.getRegisteredOn().toString())
                .setEmail(profile.getEmail())
                .setPhoneNumber(profile.getPhoneNumber())
                .setStatus(UserStatus.valueOf(user.getStatus().name()))
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
            LocalDate birthday = LocalDate.parse(request.getBirthday());
            profile.setBirthday(birthday);
        }
        if (!request.getEmail().isEmpty()) {
            profile.setEmail(request.getEmail());
        }
        if (!request.getPhoneNumber().isEmpty() &&
                !validator.isPhoneValid(request.getPhoneNumber())) {
            log.error("phone number {} is not VALID", request.getPhoneNumber());
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription("Invalid phonenumber format. Use +380123456789 pattern with 10-15 digits")
                            .asRuntimeException()
            );
        } else {
            profile.setPhoneNumber(request.getPhoneNumber());
        }
        if (!request.getStatus().name().isEmpty()) {
            user.setStatus(User.UserStatus.valueOf(request.getStatus().name()));
        }
        userRepository.save(user);
        UserResponse response = UserResponse.newBuilder()
                .setUserId(user.getUserId())
                .setName(profile.getName())
                .setSurname(profile.getSurname())
                .setBirthday(new SimpleDateFormat(DD_MM_YYYY).format(profile.getBirthday()))
                .setRegisteredOn(new SimpleDateFormat(DD_MM_YYYY).format(profile.getRegisteredOn()))
                .setEmail(profile.getEmail())
                .setPhoneNumber(profile.getPhoneNumber())
                .setStatus(UserStatus.valueOf(user.getStatus().name()))
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
                .setBirthday(stringifyDate(profile.getBirthday()))
                .setRegisteredOn(profile.getRegisteredOn().toString())
                .setEmail(profile.getEmail())
                .setPhoneNumber(profile.getPhoneNumber())
                .setStatus(UserStatus.valueOf(user.getStatus().name()))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


    private LocalDate parseDate(String date) {
        try {
            return LocalDate.parse(date, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw Status.INVALID_ARGUMENT
                    .withDescription("Invalid birthday format. Expected dd-MM-yyyy")
                    .asRuntimeException();
        }
    }

    private String stringifyDate(LocalDate date) {
        return date.toString();
    }


}




