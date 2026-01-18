package com.koi151.money.fintrack.core.user;

import com.koi151.money.fintrack.core.user.domain.User;
import com.koi151.money.fintrack.core.user.payload.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    UserResponse toResponse(User user);
}
